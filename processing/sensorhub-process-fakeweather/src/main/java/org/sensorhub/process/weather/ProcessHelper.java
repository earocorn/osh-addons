package org.sensorhub.process.weather;

import net.opengis.gml.v32.impl.ReferenceImpl;
import net.opengis.sensorml.v20.AbstractProcess;
import net.opengis.sensorml.v20.AggregateProcess;
import net.opengis.sensorml.v20.IOPropertyList;
import net.opengis.sensorml.v20.impl.SettingsImpl;
import net.opengis.swe.v20.AbstractSWEIdentifiable;
import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.DataRecord;
import org.slf4j.Logger;
import org.vast.process.ExecutableProcessImpl;
import org.vast.process.ProcessException;
import org.vast.sensorML.*;
import org.vast.xml.XMLWriterException;

import java.io.OutputStream;

public class ProcessHelper extends SMLUtils {
    AggregateProcessImpl aggregateProcess;
    ReferenceImpl controlType;
    ReferenceImpl sourceType;
    public ProcessHelper() {
        super(V2_0);

        controlType = new ReferenceImpl("urn:osh:process:datasink:commandstream");
        sourceType = new ReferenceImpl("urn:osh:process:datasource:stream");

        aggregateProcess = new AggregateProcessImpl();
    }
    public ProcessHelper(SMLStaxBindings staxBindings) {
        super(staxBindings);
    }

    /**
     * Prints XML process description to output stream
     *
     * @param outputStream
     */
    public void writeXML(OutputStream outputStream) throws XMLWriterException {
        writeProcess(outputStream, aggregateProcess, true);
    }

    public ProcessChainBuilder createProcessChain() {
        return new ProcessChainBuilder();
    }

    //        helper.createProcessChain()
//                // auto generate UUID
//                .name("Process Chain")
//                // Uid for inner aggregate process
//                .uid("urn:osh:process:weather")
//                .addDataSource("source0", "urn:osh:sensor:fakeweather:001")
//                .addOutputList(outputs)
//                //.addOutput("output1", output)
//                .addProcess("process0", processImplementation)
//                .addConnection("components/process0/outputs/output1",
//                        "outputs/output1")
//                .addConnection("", "")
    //            .validate()
//                .build();

    public class ProcessChainBuilder {
        ProcessHelper helper;
        ProcessChainBuilder() {
            helper = new ProcessHelper();
        }

        public ProcessChainBuilder uid(String uid) {
            helper.aggregateProcess.setUniqueIdentifier(uid);
            return this;
        }

        public ProcessChainBuilder name(String name) {
            helper.aggregateProcess.setName(name);
            return this;
        }

        /**
         * Adds output to aggregate process
         *
         * @param output DataRecord that describes output
         */
        public ProcessChainBuilder addOutput(DataRecord output) {
            helper.aggregateProcess.addOutput(output.getName(), output);
            return this;
        }

        public ProcessChainBuilder addOutput(String name, DataRecord output) {
            helper.aggregateProcess.addOutput(name, output);
            return this;
        }

        /**
         * Adds output list to aggregate process
         *
         * @param outputs List of outputs from a process
         */
        public ProcessChainBuilder addOutputList(IOPropertyList outputs) {
            for (AbstractSWEIdentifiable output : outputs) {
                DataComponent outputData = (DataComponent) output;
                helper.aggregateProcess.addOutput(outputData.getName(), outputData);
            }
            return this;
        }

        /**
         * Adds input to aggregate process
         *
         * @param input DataRecord that describes input
         */
        public ProcessChainBuilder addInput(DataRecord input) {
            helper.aggregateProcess.addInput(input.getName(), input);
            return this;
        }

        public ProcessChainBuilder addInput(String name, DataRecord input) {
            helper.aggregateProcess.addInput(name, input);
            return this;
        }

        /**
         * Adds process to aggregate process
         *
         * @param process Class of process
         */
        public ProcessChainBuilder addProcess(String name, ExecutableProcessImpl process) throws ProcessException {
            process.init();
            SimpleProcessImpl execProcess = new SimpleProcessImpl();
            execProcess.setExecutableImpl(process);

            helper.aggregateProcess.addComponent(name, execProcess);
            return this;
        }

        /**
         * Adds datasource to aggregate process
         *
         * @param systemUID System UID of datasource
         */
        public ProcessChainBuilder addDataSource(String name, String systemUID) {
            SimpleProcessImpl source = new SimpleProcessImpl();
            source.setTypeOf(sourceType);
            SettingsImpl settings = new SettingsImpl();
            settings.addSetValue("parameters/producerURI", systemUID);
            source.setConfiguration(settings);

            helper.aggregateProcess.addComponent(name, source);
            return this;
        }

        /**
         * Adds control stream to aggregate process
         *
         * @param systemUID System UID of control stream
         * @param inputName Name of control stream input
         */
        public ProcessChainBuilder addControlStream(String name, String systemUID, String inputName) {
            SimpleProcessImpl control = new SimpleProcessImpl();
            control.setTypeOf(controlType);
            SettingsImpl settings = new SettingsImpl();
            settings.addSetValue("parameters/systemUID", systemUID);
            settings.addSetValue("parameters/inputName", inputName);

            control.setConfiguration(settings);

            helper.aggregateProcess.addComponent(name, control);
            return this;
        }

        /**
         * Adds connection to link inputs to outputs or vice-versa
         *
         * @param source String of source of connection
         * @param destination String of destination of connection
         */
        public ProcessChainBuilder addConnection(String source, String destination) {
            helper.aggregateProcess.addConnection(new LinkImpl(source, destination));
            return this;
        }

        public AggregateProcess build() {
            return helper.aggregateProcess;
        }

    }

    public void initProcessChain(AbstractProcess processChain, boolean useThreads, Logger logger) throws ProcessException, SMLException {
        AggregateProcessImpl chain = (AggregateProcessImpl)this.getExecutableInstance((AggregateProcessImpl)processChain, useThreads);
        chain.setInstanceName("chain");
        chain.setParentLogger(logger);
        chain.init();
    }

}