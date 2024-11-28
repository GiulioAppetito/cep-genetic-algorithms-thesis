module flinkCEP.Patterns {
    requires kryo;
    requires java.base;
    requires java.sql;
    requires io.github.ericmedvet.jgea.core;
    requires io.github.ericmedvet.jgea.experimenter;
    requires io.github.ericmedvet.jnb.core;
    // requires flink.streaming.java;
    // requires flink.core;
    requires org.apache.commons.csv;
    requires flink.cep;
    requires merged.jar;
    opens events to kryo;
    opens problem to io.github.ericmedvet.jnb.core;
}