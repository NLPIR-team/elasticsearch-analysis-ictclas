package com.lingjoin.elasticsearch.index;

import com.lingjoin.nlpir.NlpirException;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.elasticsearch.Version;
import org.elasticsearch.cluster.metadata.IndexMetadata;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.analysis.AnalysisTestsHelper;
import org.elasticsearch.index.analysis.IndexAnalyzers;
import org.elasticsearch.index.analysis.NamedAnalyzer;
import org.elasticsearch.index.analysis.TokenizerFactory;
import org.elasticsearch.test.ESTestCase;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;

import static org.hamcrest.Matchers.*;

public class IctclasAnalysisTests extends ESTestCase {
    public void testDefaultsAnalysis() throws IOException {
        TestAnalysis analysis = createTestAnalysis();

        TokenizerFactory tokenizerFactory = analysis.tokenizer.get("ictclas_index");
        assertThat(tokenizerFactory, instanceOf(IctclasTokenizerFactory.class));

        IndexAnalyzers indexAnalyzers = analysis.indexAnalyzers;
        NamedAnalyzer analyzer = indexAnalyzers.get("ictclas_index");
        assertThat(analyzer.analyzer(), instanceOf(IctclasAnalyzer.class));

        assertThat(analyzer.analyzer().tokenStream(null, new StringReader("")), instanceOf(IctclasTokenizer.class));

    }

//    public void testBaseFormFilterFactory() throws IOException, NlpirException {
//        String source = "一切有权力的人都容易滥用权力，这是一条千古不变的经验";
//        String[] expected = new String[]{"一切", "有", "权力", "的", "人", "都", "容易", "滥用", "权力", "，", "这", "是", "一", "条", "千古", "不", "变", "的", "经验"};
//
//        Tokenizer tokenizer = new IctclasTokenizer("packaging/", 1, "", null, false, false);
//        tokenizer.setReader(new StringReader(source));
//        assertSimpleTSOutput(tokenizer, expected);
//    }

    public static void assertSimpleTSOutput(TokenStream stream, String[] expected) throws IOException {
        stream.reset();
        CharTermAttribute termAttr = stream.getAttribute(CharTermAttribute.class);
        assertThat(termAttr, notNullValue());
        int i = 0;
        while (stream.incrementToken()) {
            assertThat(expected.length, greaterThan(i));
            assertThat("expected different term at index " + i, termAttr.toString(), equalTo(expected[i++]));
        }
        assertThat("not all tokens produced", i, equalTo(expected.length));
    }

    private TestAnalysis createTestAnalysis() throws IOException {
        Path home = createTempDir();
        Settings settings = Settings.builder().put(IndexMetadata.SETTING_VERSION_CREATED, Version.CURRENT).put(Environment.PATH_HOME_SETTING.getKey(), home).build();
        return AnalysisTestsHelper.createTestAnalysisFromSettings(settings, new IctclasAnalysisPlugin());
    }
}
