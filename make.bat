if "%1"=="" goto javac
if "%1"=="all" goto clean
goto %1

:clean

del *.class
del *.bak
del jp\gr\java_conf\turner\util\lha\*.class
del jp\gr\java_conf\turner\util\lha\*.bak
del ..\KissChoco\jp\gr\java_conf\turner\util\lha\*.class
del ..\KissChoco\jp\gr\java_conf\turner\util\lha\*.bak
deltree /y docs
del readme.euc
del jlhasrc.LZH

if NOT "%1"=="all" goto end
goto release

:javac

rem javac -g -J-Djavac.pipe.output=true @javafiles >err
oldjavac -g -J-Djavac.pipe.output=true @javafiles >err
call hm err

if NOT "%1"=="all" goto end

:release

rem javac -d ..\KissChoco -J-Djavac.pipe.output=true @javafiles >err
rem javac -J-Djavac.pipe.output=true lhax.java @javafiles >>err
oldjavac -d ..\KissChoco -J-Djavac.pipe.output=true @javafiles >err
oldjavac -J-Djavac.pipe.output=true lhax.java @javafiles >>err
call hm err

if NOT "%1"=="all" goto end

:docs

if not exist docs mkdir docs
javadoc -public -sourcepath . -d docs -doctitle LHAƒ‰ƒCƒuƒ‰ƒŠ -nodeprecatedlist -nohelp jp.gr.java_conf.turner.util.lha
nkf32 -e -d -O readme.sjis readme.euc

if NOT "%1"=="all" goto end

:zip

del jlhasrc.LZH
lha u -rx jlhasrc lhax.java @javafiles

goto end

:makearc
if "%2"=="" goto end
cd ..

jar cvfM %2 jlha/readme.sjis jlha/readme.euc jlha/jlhasrc.LZH jlha/lhax.class jlha/jp/gr/java_conf/turner/util/lha/*.class jlha/docs

cd jlha

:end
