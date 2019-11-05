package jp.posl.jprophet.operation;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.Optional;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.Range;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.BreakStmt;

import jp.posl.jprophet.RepairUnit;
import jp.posl.jprophet.AstGenerator;

/**
 * 対象ステートメントの以前に現れているステートメントを，
 * 対象ステートメントの直前に挿入し，さらに置換操作(ValueReplacementOperation)を適用する．
 */
public class CopyReplaceOperation implements AstOperation{

    private final RepairUnit repairUnit;
    private final Node targetNode;

    public CopyReplaceOperation(RepairUnit repairUnit){
        this.repairUnit = repairUnit;
        this.targetNode = this.repairUnit.getTargetNode();
    }
    public List<RepairUnit> exec(){
        List<RepairUnit> candidates = new ArrayList<RepairUnit>();
        //修正対象のステートメントの属するメソッドノードを取得
        //メソッド内のステートメント(修正対象のステートメントより前のもの)を収集
        List<Statement> statements = collectLocalStatements(this.targetNode);
        if (statements.size() != 0){
            candidates = copyStatementBeforeTarget(statements, this.repairUnit);
        }
        //修正対象のステートメントの直前に,収集したステートメントのNodeを追加
        return candidates;
    }

    private List<Statement> collectLocalStatements(Node targetNode){
        MethodDeclaration methodNode;
        try {
            methodNode =  targetNode.findParent(MethodDeclaration.class).orElseThrow();
        }
        catch (NoSuchElementException e) {
            return new ArrayList<Statement>();
        }

        List<Statement> localStatements = methodNode.findAll(Statement.class);

        //this.targetStatementを含むそれより後ろの行の要素を全て消す
        //BlockStmtを全て除外する
        return localStatements.stream()
            .filter(s -> getEndLineNumber(s).orElseThrow() < getBeginLineNumber(targetNode).orElseThrow())
            .filter(s -> (s instanceof BlockStmt) == false)
            .collect(Collectors.toList());
    }

    private List<RepairUnit> copyStatementBeforeTarget(List<Statement> statements, RepairUnit repairUnit){
        Node targetNode = repairUnit.getTargetNode();
        List<RepairUnit> candidates = new ArrayList<RepairUnit>();

        BlockStmt blockStmt;
        try {
            blockStmt =  targetNode.findParent(BlockStmt.class).orElseThrow();
        }
        catch (NoSuchElementException e) {
            return candidates;
        }
        
        for (Statement statement : statements){            
            NodeList<Statement> nodeList = blockStmt.clone().getStatements();
            if(targetNode instanceof Statement && nodeList.indexOf(targetNode) != -1){
                NodeList<Statement> newNodeList = nodeList.addBefore(statement, (Statement)targetNode);
                RepairUnit newCandidate = RepairUnit.copy(repairUnit);
                newCandidate.getTargetNode().getParentNode().orElseThrow().replace(newNodeList.get(nodeList.indexOf(targetNode)).getParentNode().orElseThrow());
                candidates.add(newCandidate);
            }
        }
        return candidates;
    }

    private Optional<Integer> getBeginLineNumber(Node node) {
        try {
            Range range = node.getRange().orElseThrow();        
            return Optional.of(range.begin.line);
        } catch (NoSuchElementException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private Optional<Integer> getEndLineNumber(Node node) {
        try {
            Range range = node.getRange().orElseThrow();        
            return Optional.of(range.end.line);
        } catch (NoSuchElementException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }
}