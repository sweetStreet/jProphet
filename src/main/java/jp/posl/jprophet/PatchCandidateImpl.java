package jp.posl.jprophet;

import java.util.NoSuchElementException;
import java.util.Optional;

import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;

/**
 * 実際にプログラムの生成が可能なパッチ候補の実装クラス
 */
public class PatchCandidateImpl implements PatchCandidate {
    private final Node fixedNode; 
    private final CompilationUnit compilationUnit;
    private final String fixedFilePath;
    private final String fixedFileFqn;

    /**
     * 以下の引数の情報を元にパッチ候補を生成 
     * @param repairUnit 修正されたASTノードの情報を持つRepairUnit
     * @param fixedFilePath 修正されたファイルのパス（jprophetルートからの相対パス）
     * @param fixedFileFQN 修正されたファイルのFQN
     */
    public PatchCandidateImpl(RepairUnit repairUnit, String fixedFilePath, String fixedFileFQN) {
        this.fixedNode = repairUnit.getTargetNode();
        this.compilationUnit = repairUnit.getCompilationUnit();
        this.fixedFilePath = fixedFilePath;
        this.fixedFileFqn = fixedFileFQN;
    }

    /**
     * {@inheritDoc}
     */
    public String getFilePath(){
        return this.fixedFilePath;
    }

    /**
     * {@inheritDoc}
     */
    public String getFqn(){
        return this.fixedFileFqn;
    }

    /**
     * {@inheritDoc}
     */
    public CompilationUnit getCompilationUnit(){
        return this.compilationUnit;
    }

    /**
     * {@inheritDoc}
     */
    public Optional<Integer> getLineNumber() {
        try {
            Range range = this.fixedNode.getRange().orElseThrow();        
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
    public String toString(){
        return this.compilationUnit.toString();
    }
}
