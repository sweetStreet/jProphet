package jp.posl.jprophet.evaluator;

import java.util.List;
import java.util.stream.Collectors;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.ContinueStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.WhileStmt;

import jp.posl.jprophet.NodeUtility;

/**
 * AST情報を基にプログラムの各行のステートメントの特徴を抽出するクラス
 */
public class StatementFeatureExtractor {
    /**
     * 特徴抽出を行う 
     * @param line 特徴抽出したい行番号
     * @param node 特徴抽出する行をプログラムのASTノード
     * @return ステートメントの特徴ベクトル
     */
    public StatementFeatureVec extract(int line, Node node) {
        List<Node> nodes = NodeUtility.getAllNodesInDepthFirstOrder(node);
        List<Node> nodesInTheLine = nodes.stream()
            .filter(n ->  {
                if(!n.getBegin().isPresent()) return false;
                return n.getBegin().get().line == line;
            })
            .collect(Collectors.toList());

        StatementFeatureVec vec = new StatementFeatureVec();
        for(Node nodeInTheLine: nodesInTheLine) {
            if(nodeInTheLine instanceof AssignExpr) {
                vec.assignStmt += 1;
            }
            if(nodeInTheLine instanceof MethodCallExpr) {
                vec.methodCallStmt += 1;
            }
            // Streamは未対応
            if(nodeInTheLine instanceof ForStmt || nodeInTheLine instanceof WhileStmt || nodeInTheLine instanceof ForeachStmt) {
                vec.loopStmt += 1;
            }
            if(nodeInTheLine instanceof IfStmt) {
                vec.ifStmt += 1;
            }
            if(nodeInTheLine instanceof ReturnStmt) {
                vec.returnStmt += 1;
            }
            if(nodeInTheLine instanceof BreakStmt) {
                vec.breakStmt += 1;
            }
            if(nodeInTheLine instanceof ContinueStmt) {
                vec.continueStmt += 1;
            }
        }
        return vec;
    }    
}