package topology;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import org.apache.storm.daemon.metrics.reporters.PreparableReporter;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GraphiteClusterMetricsReporter implements PreparableReporter {

    GraphiteReporter reporter = null;

    public void prepare(MetricRegistry metricsRegistry, Map<String, Object> daemonConf) {

        Graphite graphite = new Graphite(new InetSocketAddress("graphite", 2003));

        reporter = GraphiteReporter.forRegistry(metricsRegistry)
                .prefixedWith("storm-cluster.metrics")
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .filter((name, metric) -> true)
                .build(graphite);
    }

    public void start() {
        if (this.reporter != null) {
            this.reporter.start(30, TimeUnit.SECONDS);
        } else {
            throw new IllegalStateException("Attempt to start without preparing " + this.getClass().getSimpleName());
        }
    }

    public void stop() {
        if (this.reporter != null) {
            this.reporter.report();
            this.reporter.stop();
        } else {
            throw new IllegalStateException("Attempt to stop without preparing " + this.getClass().getSimpleName());
        }
    }
}
