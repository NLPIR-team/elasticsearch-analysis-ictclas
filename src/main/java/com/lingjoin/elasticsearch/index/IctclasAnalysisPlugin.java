package com.lingjoin.elasticsearch.index;

import com.lingjoin.elasticsearch.util.Access;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.io.stream.NamedWriteableRegistry;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.NodeEnvironment;
import org.elasticsearch.index.analysis.AnalyzerProvider;
import org.elasticsearch.index.analysis.TokenizerFactory;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.repositories.RepositoriesService;
import org.elasticsearch.script.ScriptService;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.watcher.ResourceWatcherService;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * The type Ictclas analysis plugin.
 */
public class IctclasAnalysisPlugin extends Plugin implements AnalysisPlugin {
    /**
     * The constant LOGGER.
     */
    public static final Logger LOGGER = LogManager.getLogger();

    /**
     * The constant PLUGIN_NAME.
     */
    public static final String PLUGIN_NAME = "analysis-ictclas";

    // TODO Filter 停用词
    @Override
    public Map<String, AnalysisModule.AnalysisProvider<TokenizerFactory>> getTokenizers() {
        Map<String, AnalysisModule.AnalysisProvider<TokenizerFactory>> extra = new HashMap<>();
        extra.put("ictclas_search", IctclasTokenizerFactory::getIctclasSearchTokenizerFactory);
        extra.put("ictclas_index", IctclasTokenizerFactory::getIctclasIndexTokenizerFactory);
        return extra;
    }

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> getAnalyzers() {
        Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> extra = new HashMap<>();
        extra.put("ictclas_search", IctclasAnalyzerProvider::getIctclasSearchAnalyzerProvider);
        extra.put("ictclas_index", IctclasAnalyzerProvider::getIctclasIndexAnalyzerProvider);
        return extra;
    }

    @Override
    public Collection<Object> createComponents(Client client, ClusterService clusterService, ThreadPool threadPool, ResourceWatcherService resourceWatcherService, ScriptService scriptService, NamedXContentRegistry xContentRegistry, Environment environment, NodeEnvironment nodeEnvironment, NamedWriteableRegistry namedWriteableRegistry, IndexNameExpressionResolver indexNameExpressionResolver, Supplier<RepositoriesService> repositoriesServiceSupplier) {
        // es权限, 设置系统变量, JNA使用elasticsearch临时文件夹
        IctclasAnalysisPlugin.LOGGER.info("Set jna.tmpdir in IctclasAnalysisPlugin");
        Access.doPrivileged(() -> System.setProperty("jna.tmpdir", environment.tmpFile().toString()));
        return super.createComponents(client, clusterService, threadPool, resourceWatcherService, scriptService, xContentRegistry, environment, nodeEnvironment, namedWriteableRegistry, indexNameExpressionResolver, repositoriesServiceSupplier);
    }
}
