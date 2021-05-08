#!/bin/bash -e

#--------------------------------------------
# 发布用工具脚本
# author：zhang lei
#--------------------------------------------

# 仓库地址
GIT_ADDRESS="git@github.com:coolbeevip/license-maven-plugin.git"
PROJECT_NAME="license-maven-plugin"

## 增加版本号
## $1: 当前版本号
## $2: 版本号增加位置: 0 – major, 1 – minor, 2 – patch
increment_version() {
  local delimiter=.
  local array=($(echo "$1" | tr $delimiter '\n'))
  array[$2]=$((array[$2]+1))
  echo $(local IFS=$delimiter ; echo "${array[*]}")
}

## 发布前检查
release_check(){
  echo "5.发布前分支以及TAG检查"
  if git branch -a | grep remotes/origin/$RELEASE_BRANCH ; then
    echo "发布前检查失败：分支 $RELEASE_BRANCH 已存在，你可使用以下命令删除已存在的分支：" ;
    echo "git branch -D $RELEASE_BRANCH"
    echo "git push origin --delete $RELEASE_BRANCH"
    exit 1
  fi

  if git tag | grep $RELEASE_VERSION ; then
    echo "发布前检查失败：TAG $RELEASE_VERSION 已存在, 你可以使用以下命令删除已存在的 TAG" ;
    echo "git tag -d $RELEASE_VERSION"
    echo "git push origin :refs/tags/$RELEASE_VERSION"
    exit 1
  fi
}

release_func(){
  echo "6.开始发布"
  echo "创建 $RELEASE_BRANCH 分支......"
  git checkout -b $RELEASE_BRANCH
  git push origin $RELEASE_BRANCH

  echo "创建 $RELEASE_VERSION TAG......"
  git checkout master
  ./mvnw versions:set-property -Dproperty=version -DnewVersion=$RELEASE_VERSION
  ./mvnw versions:commit
  git commit -a -m "Upgrade Version to $RELEASE_VERSION"
  git tag -a $RELEASE_VERSION -m "Release $RELEASE_VERSION"
  git push origin $RELEASE_VERSION

  echo "更新主干版本号为 $NEXT_VERSION..."
  ./mvnw versions:set-property -Dproperty=version -DnewVersion=$NEXT_VERSION
  ./mvnw versions:commit
  git commit -a -m "Upgrade Release Version $NEXT_VERSION"
  git push origin master
  echo "发布成功，请到仓库中检查相关分支、TAG是否正确，主干和TAG将会自动触发 CI 编译发布，请确认是否发布成功"
}

main(){
  WORK_DIR=$(dirname $(mktemp -u))
  echo "1.初始化工作目录 $WORK_DIR"
  if [ ! -d $WORK_DIR ]; then
    mkdir -p $WORK_DIR
  fi

  echo "2.拉取仓库代码 $GIT_ADDRESS"
  cd $WORK_DIR
  rm -rf $PROJECT_NAME
  git clone $GIT_ADDRESS

  echo "3.测试代码"
  cd $PROJECT_NAME
  ./mvnw clean package

  echo "4.计算版本发布信息"
  # 读取当前项目版本
  CURRENT_VERSION=$(mvn -q -Dexec.executable=echo -Dexec.args='${project.version}' --non-recursive exec:exec)

  # 计算当前版本的维护分支名
  RELEASE_BRANCH=${CURRENT_VERSION//0-SNAPSHOT/X}

  # 计算当前版本的发布 TAG 名
  RELEASE_VERSION=${CURRENT_VERSION//-SNAPSHOT/}

  # 计算下一个版本号
  NEXT_VERSION=$(increment_version $RELEASE_VERSION 1)-SNAPSHOT

  echo "===================================================================="
  echo "发布工作目录: $WORK_DIR/nc"
  echo "当前版本号: $CURRENT_VERSION"
  echo "版本维护分支名: $RELEASE_BRANCH"
  echo "发布版本号: $RELEASE_VERSION"
  echo "新版本号: $NEXT_VERSION"
  echo "===================================================================="
  while true
  do
    read -p "下一步将创建 $RELEASE_BRANCH 分支, $RELEASE_VERSION TAG, 并将主干版本号改为 $NEXT_VERSION, 确认请输入(Y/N): " input

    case $input in
        [yY])
        release_check
        release_func
        exit 0
        ;;

        [nN])
        exit 1
        ;;

        *)
        echo "无效的输入"
        ;;
    esac
  done
}

main "$@"
