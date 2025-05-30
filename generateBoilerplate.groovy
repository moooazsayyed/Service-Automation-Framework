def call(Map args) {
  def type = args.type
  def repoName = args.repo

  echo "Generating boilerplate for ${type} in repo ${repoName}"

  def templatePath = ''
  switch (type) {
    case 'service':
      templatePath = 'templates/service'
      break
    case 'sdk':
      templatePath = 'templates/sdk'
      break
    case 'cloud_function':
      templatePath = 'templates/cloud_function'
      break
    default:
      error "Unknown service type: ${type}"
  }

  // Simulate copying files to the new repo directory (you can use git CLI or APIs for real cloning)
  sh """
    mkdir -p ${repoName}
    cp -r ${templatePath}/* ${repoName}/
  """

  echo "Boilerplate created at ./${repoName}/"
}



def call(Map args) {
  def repoName = args.name
  def org = args.org ?: 'your-org-name'

  echo "Creating GitHub repo ${org}/${repoName}"

  // Requires GitHub CLI (`gh`) setup with auth
  sh """
    gh repo create ${org}/${repoName} --public --confirm --source=./${repoName}
    cd ${repoName}
    git init
    git add .
    git commit -m "Initial commit"
    git branch -M main
    git remote add origin https://github.com/${org}/${repoName}.git
    git push -u origin main
  """

  echo "GitHub repo ${repoName} created and pushed."
}


def call(Map args) {
  def repo = args.repo

  echo "Applying code rules to ${repo}"

  // Simulate applying repo standards: branch protections, linters, etc.
  sh """
    cd ${repo}
    echo '# Standard PR template' > .github/pull_request_template.md
    echo 'rules: enforce linting, format' > .github/code_rules.yml
    git add .github/*
    git commit -m "Add code standards and templates"
    git push
  """

  echo "Standard templates added to ${repo}"
}


def call(Map args) {
  def env = args.env
  def repo = args.service
  def kubeCredId = args.kubeCredId

  echo "Deploying ${repo} to Kubernetes in ${env} namespace"

  withCredentials([file(credentialsId: kubeCredId, variable: 'KUBECONFIG')]) {
    sh """
      export KUBECONFIG=$KUBECONFIG
      kubectl config use-context ${env}-context
      kubectl apply -f ${repo}/k8s/${env}/
    """
  }

  echo "Deployment successful to ${env} environment"
}


def call(Map args) {
  def functionName = args.functionName
  def env = args.env

  echo "Deploying ${functionName} as AWS Lambda for ${env}"

  sh """
    cd ${functionName}
    zip function.zip * -r
    aws lambda update-function-code \
      --function-name ${functionName}-${env} \
      --zip-file fileb://function.zip
  """

  echo "Lambda function ${functionName} deployed to ${env}"
}
