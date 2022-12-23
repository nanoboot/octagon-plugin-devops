#! /bin/bash

set -e

currentDir=`pwd`

### Set, if needed:
pomParentDir=.

stepCount=13

fromStepCount="/"$stepCount

echo "### Release of new version of a Maven artifact ###"


read -p "Current version (to be released now, example: 1.2.3):" currentVersion
echo "Current version=" $currentVersion

read -p "Next version (example: 1.2.3):" nextVersion
echo "Next version=" nextVersion

echo Step : 1$fromStepCount : Deploy current snapshot to Maven repository
cd $pomParentDir
mvn clean deploy&&echo "DONE"
cd $currentDir

echo Step : 2$fromStepCount : Delete -SNAPSHOT- all occurences in poms

echo "Note: replace " $currentVersion-SNAPSHOT" by " $currentVersion "in all occurrences for this artifact:"
read -p "Have you deleted all the occurences (y/n)?" choice
case "$choice" in
  y|Y ) echo "yes"&&echo "DONE";;
  n|N ) echo "no";;
  * ) echo "invalid"&&exit;;
esac

echo Step : 3$fromStepCount : Deploy released version to Maven repository
cd $pomParentDir
mvn clean deploy&&echo "DONE"
cd $currentDir

echo Step : 4$fromStepCount : Add the deletion to git
git add .&&echo "DONE"


echo Step : 5$fromStepCount : Commit the released version to git
git commit -m "Released $currentVersion"&&echo "DONE"


#git tag -d 0.0.0
#git push --delete origin 0.0.0

echo Step : 6$fromStepCount : Tag released version
#git tag -a $currentVersion -m "$currentVersion"&&echo "DONE"
git tag $currentVersion&&echo "DONE"


echo Step : 7$fromStepCount : Push to git
#git push -f origin HEAD;
git push origin HEAD&&echo "DONE"


echo Step : 8$fromStepCount : Push new tag to git
git push origin $currentVersion&&echo "DONE"

echo Step : 9$fromStepCount : Increase version and add -SNAPSHOST  \(example: 999.999.999-SNAPSHOT\)
echo "Note: replace " $currentVersion " by " $nextVersion"-SNAPSHOT"

read -p "Have you replaced all the occurrences (y/n)?" choice
case "$choice" in
  y|Y ) echo "yes"&&echo "DONE";;
  n|N ) echo "no";;
  * ) echo "invalid"&&exit;;
esac

echo Step : 10$fromStepCount : Adding the changes to git
git add .&&echo "DONE"

echo Step : 11$fromStepCount : Commit the new snapshot version to git

git commit -m "Released "$nextVersion"-SNAPSHOT"&&echo "DONE"

echo Step : 12$fromStepCount : Push to git
git push origin HEAD&&echo "DONE"


echo Step : 13$fromStepCount : Deploy the new snapshot to Maven repository
cd $pomParentDir
mvn clean deploy&&echo "DONE"
cd $currentDir

echo "RELEASE OF NEW VERSION $currentVersion WAS JUST FINISHED"

echo "NOW UPDATE DATA IN OCTAGON."

echo "EXITING SCRIPT"

gitk --all
