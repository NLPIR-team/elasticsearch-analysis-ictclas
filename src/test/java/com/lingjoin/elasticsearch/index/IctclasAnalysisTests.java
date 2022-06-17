package com.lingjoin.elasticsearch.index;

import org.apache.lucene.analysis.TokenStream;
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
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;

import static org.hamcrest.Matchers.*;

public class IctclasAnalysisTests extends ESTestCase {
    public void testDefaultsAnalysis() throws IOException {

        TestAnalysis analysis = createTestAnalysis();

        TokenizerFactory tokenizerIndexFactory = analysis.tokenizer.get("ictclas_index");
        assertThat(tokenizerIndexFactory, instanceOf(IctclasTokenizerFactory.class));
        TokenizerFactory tokenizerSearchFactory = analysis.tokenizer.get("ictclas_search");
        assertThat(tokenizerSearchFactory, instanceOf(IctclasTokenizerFactory.class));


        IndexAnalyzers analyzers = analysis.indexAnalyzers;
        NamedAnalyzer indexAnalyzer = analyzers.get("ictclas_index");
        assertThat(indexAnalyzer.analyzer(), instanceOf(IctclasAnalyzer.class));
        NamedAnalyzer searchAnalyzer = analyzers.get("ictclas_search");
        assertThat(searchAnalyzer.analyzer(), instanceOf(IctclasAnalyzer.class));


        assertThat(indexAnalyzer.analyzer().tokenStream(null, new StringReader("")), instanceOf(IctclasTokenizer.class));

        String source = "一切有权力的人都容易滥用权力，这是一条千古不变的经验";
        String[] expected = new String[]{"一切", "有", "权力", "的", "人", "都", "容易", "滥用", "权力", "，", "这", "是", "一", "条", "千古", "不", "变", "的", "经验"};

        try (TokenStream indexTokenStream = indexAnalyzer.analyzer().tokenStream(null, new StringReader(source))) {
            assertSimpleTSOutput(indexTokenStream, expected);
        }

        try (TokenStream searchTokenStream = searchAnalyzer.analyzer().tokenStream(null, new StringReader(source))) {
            assertSimpleTSOutput(searchTokenStream, expected);
        }

        String sourceIr = "国务院办公厅转发商务部";
        String[] expectedIndex = new String[]{"国务院办公厅", "国务院", "办公厅", "转发", "商务部"};
        String[] expectedSearch = new String[]{"国务院办公厅", "转发", "商务部"};


        try (TokenStream indexTokenStream = indexAnalyzer.analyzer().tokenStream(null, new StringReader(sourceIr))) {
            assertSimpleTSOutput(indexTokenStream, expectedIndex);
        }

        try (TokenStream searchTokenStream = searchAnalyzer.analyzer().tokenStream(null, new StringReader(sourceIr))) {
            assertSimpleTSOutput(searchTokenStream, expectedSearch);
        }
    }

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
        Path home = Optional.ofNullable(IctclasAnalysisTests.class.getResource("/plugins"))
                .map(URL::getPath)
                .map(Path::of)
                .map(Path::getParent)
                .orElse(createTempDir());
        Settings settings = Settings.builder()
                .put(IndexMetadata.SETTING_VERSION_CREATED, Version.CURRENT)
                .put(Environment.PATH_HOME_SETTING.getKey(), home)
                .build();
        return AnalysisTestsHelper.createTestAnalysisFromSettings(settings, new IctclasAnalysisPlugin());
    }
}
