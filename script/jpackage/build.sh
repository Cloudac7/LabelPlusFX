DIR=$PWD/../..

# Clean up
if [ -d "./LabelPlusFX*" ]; then
    rm -rf ./LabelPlusFX*
fi
if [ -d "$DIR/runtime" ]; then
    rm -rf $DIR/runtime
fi

MODULES="$DIR/target/build"
ICON="$DIR/images/icons/cat.icns"

mkdir $DIR/jar
cp -r $MODULES/*.jar $DIR/jar
DEPS=`jdeps --module-path ${DIR}/jarjar \
    -quiet --multi-release 9 --print-module-deps \
    --ignore-missing-deps ${DIR}/jar/*.jar`

cd $DIR
rm -rf runtime
jlink -v --module-path $JAVA_HOME/jmods \
    --add-modules ${DEPS},jdk.crypto.cryptoki \
    --no-man-pages --no-header-files --strip-debug \
    --output runtime
cd $DIR/script/jpackage

jpackage --verbose --type app-image --app-version 2.3.3 \
    --copyright "Meodinger Tech (C) 2022" --name LabelPlusFX \
    --icon $ICON --dest . --module-path $MODULES \
    --module lpfx/ink.meodinger.lpfx.LauncherKt \
    --runtime-image ../../runtime