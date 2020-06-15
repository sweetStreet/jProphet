package jp.posl.jprophet.evaluator;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.NameExpr;

import org.junit.Test;

import jp.posl.jprophet.NodeUtility;
import jp.posl.jprophet.evaluator.VariableFeature.VarType;

public class ValueFeatureExtractorTest {
    /**
     * 宣言ノードにおける変数の型やスコープの特徴抽出のテスト
     */
    @Test public void testDeclaration() {
        final String src = new StringBuilder().append("")
            .append("public class A {\n")
            .append("    final public String str;\n")
            .append("    public void a() {\n")
            .append("        boolean bool = 0;\n")
            .append("        final int i = 0;\n")
            .append("    }\n")
            .append("}\n")
            .toString();

        Node root = NodeUtility.getAllNodesFromCode(src).get(0);
        VariableFeatureExtractor extractor = new VariableFeatureExtractor();
        List<VariableFeature> actualDeclaratorFeatures = root.findAll(VariableDeclarator.class).stream()
            .map(nameExpr -> extractor.extract(nameExpr))
            .collect(Collectors.toList());

        VariableFeature expectedFieldFeature = new VariableFeature(Set.of(
            VarType.STRING,
            VarType.OBJECT,
            VarType.FIELD
        ));
        VariableFeature expectedLocalVarFeature = new VariableFeature(Set.of(
            VarType.BOOLEAN,
            VarType.LOCAL
        ));
        VariableFeature expectedLocalConstFeature = new VariableFeature(Set.of(
            VarType.NUM,
            VarType.LOCAL,
            VarType.CONSTANT
        ));

    
        System.out.println(root.findAll(VariableDeclarator.class).get(0));
        assertThat(actualDeclaratorFeatures.get(0)).isEqualToComparingFieldByField(expectedFieldFeature);
        assertThat(actualDeclaratorFeatures.get(1)).isEqualToComparingFieldByField(expectedLocalVarFeature);
        assertThat(actualDeclaratorFeatures.get(2)).isEqualToComparingFieldByField(expectedLocalConstFeature);
    }

    /**
     * 変数がどのステートメントに含まれているか
     */
    @Test public void testValueInStmt() {
        final String src = new StringBuilder().append("")
            .append("public class A {\n")
            .append("    public void a() {\n")
            .append("        if(cond)\n")
            .append("           hoge = 0;\n")
            .append("        while(true)\n")
            .append("           fuga = 0;\n")
            .append("        for(;;)\n")
            .append("           foo = 0;\n")
            .append("        for(String str: strs)\n")
            .append("           bar = 0;\n")
            .append("        method(piyo);\n")
            .append("    }\n")
            .append("}\n")
            .toString();

        Node root = NodeUtility.getAllNodesFromCode(src).get(0);
        VariableFeatureExtractor extractor = new VariableFeatureExtractor();
        List<VariableFeature> actualFeatures = root.findAll(NameExpr.class).stream()
            .map(nameExpr -> extractor.extract(nameExpr))
            .collect(Collectors.toList());

        VariableFeature expectedFeatureInCond = new VariableFeature(Set.of(
            VarType.IN_CONDITION,
            VarType.IN_IF_STMT
        ));
        VariableFeature expectedFeatureInIfStmt = new VariableFeature(Set.of(
            VarType.IN_IF_STMT,
            VarType.IN_ASSIGN_STMT
        ));
        VariableFeature expectedFeatureInLoop = new VariableFeature(Set.of(
            VarType.IN_LOOP,
            VarType.IN_ASSIGN_STMT
        ));
        VariableFeature expectedFeatureInForeachCond = new VariableFeature(Set.of(
            VarType.IN_LOOP
        ));
        VariableFeature expectedParameterFeature = new VariableFeature(Set.of(
            VarType.PARAMETER
        ));

        assertThat(actualFeatures.get(0)).isEqualToComparingFieldByField(expectedFeatureInCond);
        assertThat(actualFeatures.get(1)).isEqualToComparingFieldByField(expectedFeatureInIfStmt);
        assertThat(actualFeatures.get(2)).isEqualToComparingFieldByField(expectedFeatureInLoop);
        assertThat(actualFeatures.get(3)).isEqualToComparingFieldByField(expectedFeatureInLoop);
        assertThat(actualFeatures.get(4)).isEqualToComparingFieldByField(expectedFeatureInForeachCond);
        assertThat(actualFeatures.get(5)).isEqualToComparingFieldByField(expectedFeatureInLoop);
        assertThat(actualFeatures.get(6)).isEqualToComparingFieldByField(expectedParameterFeature);
    }

    /**
     * 変数が可換演算中に存在する時の特徴抽出
     */
    @Test public void testValueOfCommutativeOperator() {
        final String src = new StringBuilder().append("")
            .append("public class A {\n")
            .append("    public void a() {\n")
            .append("        (a + b);\n")
            .append("        (a * b);\n")
            .append("        (a == b);\n")
            .append("        (a != b);\n")
            .append("        (a || b);\n")
            .append("        (a && b);\n")
            .append("    }\n")
            .append("}\n")
            .toString();

        Node root = NodeUtility.getAllNodesFromCode(src).get(0);
        VariableFeatureExtractor extractor = new VariableFeatureExtractor();
        List<VariableFeature> actualFeatures = root.findAll(NameExpr.class).stream()
            .map(nameExpr -> extractor.extract(nameExpr))
            .collect(Collectors.toList());

        VariableFeature expectedFeature = new VariableFeature(Set.of(
            VarType.COMMUTATIVE_OPERAND
        ));
        
        actualFeatures.stream().forEach(actual -> {
            assertThat(actual).isEqualToComparingFieldByField(expectedFeature);
        });
    }

    /**
     * 変数が二項演算中に存在する時の特徴抽出
     */
    @Test public void testValueOfBinaryOperator() {
        final String src = new StringBuilder().append("")
            .append("public class A {\n")
            .append("    public void a() {\n")
            .append("        (a - b);\n")
            .append("        (a / b);\n")
            .append("        (a % b);\n")
            .append("        (a < b);\n")
            .append("        (a > b);\n")
            .append("        (a <= b);\n")
            .append("        (a >= b);\n")
            .append("    }\n")
            .append("}\n")
            .toString();

        Node root = NodeUtility.getAllNodesFromCode(src).get(0);
        VariableFeatureExtractor extractor = new VariableFeatureExtractor();
        List<VariableFeature> actualFeatures = root.findAll(NameExpr.class).stream()
            .map(nameExpr -> extractor.extract(nameExpr))
            .collect(Collectors.toList());

        VariableFeature expectedLeftVarFeature = new VariableFeature(Set.of(
            VarType.NONCOMMUTATIVE_OPERAND_LEFT
        ));
        VariableFeature expectedRightVarFeature = new VariableFeature(Set.of(
            VarType.NONCOMMUTATIVE_OPERAND_RIGHT
        ));
        
        for(int i = 0; i < actualFeatures.size(); i += 2) {
            assertThat(actualFeatures.get(i)).isEqualToComparingFieldByField(expectedLeftVarFeature);
            assertThat(actualFeatures.get(i + 1)).isEqualToComparingFieldByField(expectedRightVarFeature);
        }
    }

    /**
     * 変数が二項演算の被演算子の子ノードに存在する時
     */
    @Test public void test() {
        final String src = new StringBuilder().append("")
            .append("public class A {\n")
            .append("    public void a() {\n")
            .append("        (a - hoge(b));\n")
            .append("    }\n")
            .append("}\n")
            .toString();

        Node root = NodeUtility.getAllNodesFromCode(src).get(0);
        VariableFeatureExtractor extractor = new VariableFeatureExtractor();
        List<VariableFeature> actualFeatures = root.findAll(NameExpr.class).stream()
            .map(nameExpr -> extractor.extract(nameExpr))
            .collect(Collectors.toList());

        VariableFeature expectedLeftVarFeature = new VariableFeature(Set.of(
            VarType.NONCOMMUTATIVE_OPERAND_LEFT
        ));
        VariableFeature expectedRightVarFeature = new VariableFeature(Set.of(
            VarType.NONCOMMUTATIVE_OPERAND_RIGHT,
            VarType.PARAMETER
        ));
        
        assertThat(actualFeatures.get(0)).isEqualToComparingFieldByField(expectedLeftVarFeature);
        assertThat(actualFeatures.get(1)).isEqualToComparingFieldByField(expectedRightVarFeature);
    }

    /**
     * 変数が単項演算中に存在する時
     */
    @Test public void testValueOfUnaryOperator() {
        final String src = new StringBuilder().append("")
            .append("public class A {\n")
            .append("    public void a() {\n")
            .append("        a++;\n")
            .append("        b--;\n")
            .append("    }\n")
            .append("}\n")
            .toString();

        Node root = NodeUtility.getAllNodesFromCode(src).get(0);
        VariableFeatureExtractor extractor = new VariableFeatureExtractor();
        List<VariableFeature> actualFeatures = root.findAll(NameExpr.class).stream()
            .map(nameExpr -> extractor.extract(nameExpr))
            .collect(Collectors.toList());

        VariableFeature expectedFeature = new VariableFeature(Set.of(
            VarType.UNARY_OPERAND
        ));
        
        actualFeatures.stream().forEach(actual -> {
            assertThat(actual).isEqualToComparingFieldByField(expectedFeature);
        });
    }
}