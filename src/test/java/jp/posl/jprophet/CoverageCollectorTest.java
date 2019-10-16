package jp.posl.jprophet;

import jp.posl.jprophet.ProjectConfiguration;

import org.junit.Before;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import jp.posl.jprophet.FL.FullyQualifiedName;
import jp.posl.jprophet.FL.TestResults;
import jp.posl.jprophet.FL.CoverageCollector;
import java.io.File;

public class CoverageCollectorTest{
    // 入力として用意するテスト用のプロジェクト
    private String projectPath;
    private ProjectConfiguration project;
    private ProjectBuilder projectBuilder = new ProjectBuilder();
    private List<String> SourceClassFilePaths = new ArrayList<String>();
    private List<String> TestClassFilePaths = new ArrayList<String>();
    private List<FullyQualifiedName> sourceClass = new ArrayList<FullyQualifiedName>();
	private List<FullyQualifiedName> testClass = new ArrayList<FullyQualifiedName>();
    private TestResults testResults = new TestResults();
    private int SCFPsize;
    private int TCFPsize;


    @Before public void setup(){
        this.projectPath = "src/test/resources/testFLProject";
        this.project = new ProjectConfiguration(this.projectPath, "./TEtmp/");
        this.SourceClassFilePaths.add("testFLProject.Forstatement");
        this.SourceClassFilePaths.add("testFLProject.Ifstatement");
        this.SourceClassFilePaths.add("testFLProject.App");


        this.TestClassFilePaths.add("testFLProject.IfstatementTest3");
        this.TestClassFilePaths.add("testFLProject.AppTest");
        this.TestClassFilePaths.add("testFLProject.IfstatementTest2");
        this.TestClassFilePaths.add("testFLProject.IfstatementTest4");
        this.TestClassFilePaths.add("testFLProject.ForstatementTest");
        this.TestClassFilePaths.add("testFLProject.IfstatementTest");

        SCFPsize = SourceClassFilePaths.size();
        TCFPsize = TestClassFilePaths.size();

        projectBuilder.build(project);
         
        for(int k = 0; k < SCFPsize; k++){
			sourceClass.add(new FullyQualifiedName(SourceClassFilePaths.get(k)));
		}
		for(int k = 0; k < TCFPsize; k++){
			testClass.add(new FullyQualifiedName(TestClassFilePaths.get(k)));
        }
    
    }

    /**
     * CoverageCollectorが動作しているかどうかのテスト
     */
    
     @Test public void testForTestExecutor(){

        CoverageCollector coverageCollector = new CoverageCollector("TEtmp");

        try{
            testResults = coverageCollector.exec(sourceClass, testClass);
        }catch (Exception e){
			System.out.println("例外");
        }
        
        //失敗,成功したテストの個数が正しいか確認
        assertThat(testResults.getFailedTestResults().size()).isEqualTo(1);
        assertThat(testResults.getSuccessedTestResults().size()).isEqualTo(9);

        List<String> Smethodlist = testResults.getSuccessedTestResults().stream()
            .map(s -> s.getMethodName().value)
            .collect(Collectors.toList());

        List<String> Fmethodlist = testResults.getFailedTestResults().stream()
            .map(s -> s.getMethodName().value)
            .collect(Collectors.toList());

        //失敗,成功したテストの一覧が正しいか確認
        assertThat(Smethodlist).contains("testFLProject.AppTest.testAppHasAGreeting");
        assertThat(Smethodlist).contains("testFLProject.ForstatementTest.Ftest0_1");
        assertThat(Smethodlist).contains("testFLProject.ForstatementTest.Ftest1_1");
        assertThat(Smethodlist).contains("testFLProject.ForstatementTest.Ftest2_2");
        assertThat(Smethodlist).contains("testFLProject.ForstatementTest.Ftest3_6");
        assertThat(Smethodlist).contains("testFLProject.IfstatementTest.test1_2");
        assertThat(Smethodlist).contains("testFLProject.IfstatementTest.test2_3");
        assertThat(Smethodlist).contains("testFLProject.IfstatementTest2.test3_4");
        assertThat(Smethodlist).contains("testFLProject.IfstatementTest4.test0_0");
        assertThat(Fmethodlist).contains("testFLProject.IfstatementTest3.test4_5");

        deleteDirectory(new File("./TEtmp/"));

    }

    /**
     * ディレクトリをディレクトリの中のファイルごと再帰的に削除する 
     * @param dir 削除対象ディレクトリ
     */
    private void deleteDirectory(File dir){
        if(dir.listFiles() != null){
            for(File file : dir.listFiles()){
                if(file.isFile())
                    file.delete();
                else
                    deleteDirectory(file);
            }
        }
        dir.delete();
    }

}