#!/bin/bash
source "$(dirname "$0")/../../scripts/setup/env_setup.sh"

# Episteme Hugging Face Deployment Script (Industrial Version)
# This script creates a lean deployment branch and pushes it to Hugging Face.

HF_REMOTE="hf"
DEPLOY_BRANCH="hf-deployment"

echo -e "\033[0;36m--- Starting Episteme HF Deployment ---\033[0m"

# 1. Ensure we are in a clean state and synced with GitHub
currentBranch=$(git rev-parse --abbrev-ref HEAD)
if [ -z "$currentBranch" ]; then echo "Error: Not in a git repo"; exit 1; fi

echo -e "\033[0;33mSyncing $currentBranch with GitHub...\033[0m"
git push origin "$currentBranch"

# 2. Create or reset the deployment branch (Orphan to save space)
echo -e "\033[0;33mCreating lean deployment branch...\033[0m"
if git branch | grep -q "$DEPLOY_BRANCH"; then
    git branch -D "$DEPLOY_BRANCH"
fi
git checkout --orphan "$DEPLOY_BRANCH"

# 3. Clear the index (it starts with all files from previous branch)
git rm -rf . --cached > /dev/null

# 4. Add ONLY the essential modules (The Scientific Engine)
echo -e "\033[0;33mAdding essential modules...\033[0m"

# Temporarily copy infrastructure folders to root for HF compatibility
cp -r infrastructure/agent agent
cp -r infrastructure/docker docker
cp infrastructure/docker/Dockerfile.huggingface Dockerfile

# Modify pom.xml on the deployment branch to only include the active modules
echo -e "\033[0;33mRewriting pom.xml to only include active modules...\033[0m"
python3 -c "import sys; p=open('pom.xml','r').read(); import re; p=re.sub(r'(?s)<modules>.*?</modules>', '  <modules>\\n    <module>episteme-core</module>\\n    <module>episteme-natural</module>\\n    <module>episteme-social</module>\\n    <module>episteme-native</module>\\n    <module>episteme-server</module>\\n  </modules>', p); open('pom.xml','w').write(p)"

git add .gitattributes
git add Dockerfile
git add pom.xml
git add README.md
git add .gitignore
git add episteme-core/
git add episteme-server/
git add episteme-natural/
git add episteme-social/
git add episteme-native/
git add agent/
git add docker/
git add libs/mpi.jar
git add libs/mpj.jar

# Remove heavy 3D models and textures rejected by Hugging Face
echo -e "\033[0;33mPruning heavy 3D assets, textures, and native binaries...\033[0m"
git rm -rf --cached episteme-natural/src/main/resources/org/episteme/natural/medicine/anatomy/models/*.fbx 2>/dev/null
git rm -rf --cached episteme-natural/src/main/resources/org/episteme/natural/physics/astronomy/*.jpg 2>/dev/null
git rm -rf --cached episteme-natural/src/main/resources/org/episteme/natural/physics/astronomy/*.tif 2>/dev/null
git rm -rf --cached episteme-core/src/main/resources/org/episteme/core/ui/*.png 2>/dev/null
git rm -rf --cached episteme-native/libs/*.dll 2>/dev/null
git rm -rf --cached episteme-native/.discovery_cache 2>/dev/null
git rm -rf --cached episteme-server/.discovery_cache 2>/dev/null

# 5. Commit the lean version
echo -e "\033[0;33mCommitting lean version...\033[0m"
git commit -m "deploy: industrial lean build for Hugging Face Spaces"

# 6. Push to Hugging Face
echo -e "\033[0;32mPushing to Hugging Face...\033[0m"
git push "$HF_REMOTE" "$DEPLOY_BRANCH:main" --force

# 7. Cleanup and return to main branch
echo -e "\033[0;33mCleaning up...\033[0m"
rm -rf agent
rm -rf docker
rm -f Dockerfile

git checkout -f "$currentBranch"
git branch -D "$DEPLOY_BRANCH"

echo -e "\033[0;32m--- Deployment Complete! ---\033[0m"
echo "Monitor your build at: https://huggingface.co/spaces/silveremartin/Episteme"
