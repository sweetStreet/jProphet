package jp.posl.jprophet.operation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.expr.BinaryExpr.Operator;
import com.github.javaparser.ast.stmt.IfStmt;

import jp.posl.jprophet.NodeUtility;
import jp.posl.jprophet.patch.DiffWithType;


/**
 * if文における条件式の変更操作
 * 条件を狭める操作と緩める操作の二つを行う
 */
public class CondRefinementOperation implements AstOperation{
    /**
     * {@inheritDoc}
     */
    public List<DiffWithType> exec(Node targetNode){
        if (!(targetNode instanceof IfStmt)) return new ArrayList<DiffWithType>();

        final DeclarationCollector collector = new DeclarationCollector();
        final List<VariableDeclarator> vars = new ArrayList<VariableDeclarator>();
        vars.addAll(collector.collectFileds(targetNode));
        vars.addAll(collector.collectLocalVarsDeclared(targetNode));
        final List<Parameter> parameters = collector.collectParameters(targetNode);

        final List<CompilationUnit> compilationUnits = new ArrayList<CompilationUnit>();
        final Expression condition = (Expression)NodeUtility.deepCopyByReparse(((IfStmt)targetNode).getCondition());
        final String abstractConditionName = "ABST_HOLE";

        this.replaceWithBinaryExprWithAbst(condition, new EnclosedExpr (new MethodCallExpr(abstractConditionName)), Operator.OR)
            .map(expr -> new ConcreteConditions(((EnclosedExpr)expr.getRight()).getInner(), vars, parameters).getCompilationUnits())
            .ifPresent(compilationUnits::addAll);
        this.replaceWithBinaryExprWithAbst(condition, new UnaryExpr (new EnclosedExpr (new MethodCallExpr(abstractConditionName)), UnaryExpr.Operator.LOGICAL_COMPLEMENT), Operator.AND)
            .map(expr -> new ConcreteConditions(((EnclosedExpr)((UnaryExpr)expr.getRight()).getExpression()).getInner(), vars, parameters).getCompilationUnits())
            .ifPresent(compilationUnits::addAll);
            
        //return compilationUnits;
        return new ArrayList<DiffWithType>();

    }

    /**
     * 条件文をexpressionからexpression operation rightExprに書き換える
     * m() -> m() && m2() など
     * @param expression 変更前の条件式
     * @param rightExpr 変更前の条件式に加える条件式
     * @param operator 比較演算子
     * @return
     */
    private Optional<BinaryExpr> replaceWithBinaryExprWithAbst(Expression expression, Expression rightExpr, Operator operator){
        final Expression condition = (Expression)NodeUtility.deepCopyByReparse(expression);
        final Expression leftExpr = new EnclosedExpr ((Expression)NodeUtility.deepCopyByReparse(expression));
        final BinaryExpr newBinaryExpr = new BinaryExpr(leftExpr, rightExpr, operator);

        return NodeUtility.replaceNode(newBinaryExpr, condition)
            .map(expr -> (BinaryExpr)expr);
    }
} 