#!/bin/bash
# Assume the names of directories do not contain '\'

if [[ $# -gt 1 ]]; then
   echo "Invalid command call:"
   echo "   $0 $*"
   echo ""
   echo "Please use the following call:"
   echo "   $0 [SCALA_VERSION]"
   exit 1
fi

if [[ $# -ge 1 ]]; then
   SCALA_VERSION="$1"
   SBT_SCALA_VERSION="++${SCALA_VERSION}"
else
   SCALA_VERSION=""
   SBT_SCALA_VERSION=""
fi

SRC_MANAGED_PATH=$(sbt ${SBT_SCALA_VERSION} "export source-managed" --error)
BUILD_PATH=$(echo "${SRC_MANAGED_PATH}" | sed 's/[\\/][^\/]*$//')
CP_PATHS=$(sbt ${SBT_SCALA_VERSION} "export test:dependency-classpath" --error)

echo "Scala version      : ${SCALA_VERSION}"
echo "Source managed path: ${SRC_MANAGED_PATH}"
echo "Build path         : ${BUILD_PATH}"
echo "CP paths           : ${CP_PATHS}"
echo ""

sbt "${SBT_SCALA_VERSION}" test:compile && \
java -cp "${CP_PATHS}" org.scalatest.tools.Runner -R "${BUILD_PATH}/test-classes" -o -PS3
