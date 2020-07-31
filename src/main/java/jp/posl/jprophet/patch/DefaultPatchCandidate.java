package jp.posl.jprophet.patch;

import java.util.NoSuchElementException;
import java.util.Optional;

import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;

import jp.posl.jprophet.NodeUtility;
import jp.posl.jprophet.operation.AstOperation;
import jp.posl.jprophet.patch.DiffWithType.ModifyType;


/**
 * 実際にプログラムの生成が可能なパッチ候補の実装クラス
 */
public class DefaultPatchCandidate implements PatchCandidate {
    private final String fixedFilePath;
    private final String fixedFileFqn;
    private Class<? extends AstOperation> operation;
    private final int id;
    private final DiffWithType diffWithType;

    /**
     * 以下の引数の情報を元にパッチ候補を生成 
     * @param targetNodeBeforeFix 修正前の対象ASTノード
     * @param fixedCompilationUnit 修正されたASTノードの情報を持つCompilationUnit
     * @param fixedFilePath 修正されたファイルのパス（jprophetルートからの相対パス）
     * @param fixedFileFQN 修正されたファイルのFQN
     * @param operation 適用されたオペレータのクラス
     */
    public DefaultPatchCandidate(DiffWithType diffWithType, String fixedFilePath, String fixedFileFQN, Class<? extends AstOperation> operation, int id) {
        this.diffWithType = diffWithType;
        this.fixedFilePath = fixedFilePath;
        this.fixedFileFqn = fixedFileFQN;
        this.operation = operation;
        this.id = id;
    }

 
    /**
     * {@inheritDoc}
     */
    @Override
    public int getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFilePath(){
        return this.fixedFilePath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFqn(){
        return this.fixedFileFqn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompilationUnit getCompilationUnit(){
        if (this.diffWithType.getModifyType().equals(ModifyType.INSERT)) {
            return NodeUtility.insertNodeWithNewLine(this.diffWithType.getTargetNodeAfterFix(), this.diffWithType.getTargetNodeBeforeFix()).get().findCompilationUnit().get();
        } else if (this.diffWithType.getModifyType().equals(ModifyType.CHANGE)) {
            return NodeUtility.replaceNode(this.diffWithType.getTargetNodeAfterFix(), this.diffWithType.getTargetNodeBeforeFix()).get().findCompilationUnit().get();
        }
        return this.diffWithType.getTargetNodeBeforeFix().findCompilationUnit().get(); 
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Integer> getLineNumber() {
        try {
            Range range = this.diffWithType.getTargetNodeBeforeFix().getRange().orElseThrow();        
            return Optional.of(range.begin.line);
        } catch (NoSuchElementException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }


    /**
     * {@inheritDoc}
     */
     @Override
     public String getAppliedOperation() {
         return operation.getName().replace("jp.posl.jprophet.operation.", "");
     }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString(){
        return new StringBuilder().append("")
            .append("ID : " + this.getId())
            .append("\n")
            .append("fixed file path : " + this.fixedFilePath)
            .append("\n")
            .append("used operation  : " + this.operation.getSimpleName())
            .append("\n\n")
            .append(new RepairDiff(this.diffWithType.getTargetNodeBeforeFix(), getCompilationUnit()).toString())
            .toString();
    }
}
