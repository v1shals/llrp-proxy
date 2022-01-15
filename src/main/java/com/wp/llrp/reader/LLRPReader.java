package com.wp.llrp.reader;

import org.apache.mina.core.session.IoSession;
import org.llrp.ltk.exceptions.InvalidLLRPMessageException;
import org.llrp.ltk.generated.enumerations.AISpecStopTriggerType;
import org.llrp.ltk.generated.enumerations.AccessReportTriggerType;
import org.llrp.ltk.generated.enumerations.AccessSpecState;
import org.llrp.ltk.generated.enumerations.AccessSpecStopTriggerType;
import org.llrp.ltk.generated.enumerations.AirProtocols;
import org.llrp.ltk.generated.enumerations.GetReaderCapabilitiesRequestedData;
import org.llrp.ltk.generated.enumerations.ROReportTriggerType;
import org.llrp.ltk.generated.enumerations.ROSpecStartTriggerType;
import org.llrp.ltk.generated.enumerations.ROSpecState;
import org.llrp.ltk.generated.enumerations.ROSpecStopTriggerType;
import org.llrp.ltk.generated.enumerations.StatusCode;
import org.llrp.ltk.generated.interfaces.AccessCommandOpSpec;
import org.llrp.ltk.generated.messages.ADD_ACCESSSPEC;
import org.llrp.ltk.generated.messages.ADD_ROSPEC;
import org.llrp.ltk.generated.messages.ADD_ROSPEC_RESPONSE;
import org.llrp.ltk.generated.messages.DELETE_ACCESSSPEC;
import org.llrp.ltk.generated.messages.DELETE_ROSPEC;
import org.llrp.ltk.generated.messages.DELETE_ROSPEC_RESPONSE;
import org.llrp.ltk.generated.messages.ENABLE_ACCESSSPEC;
import org.llrp.ltk.generated.messages.ENABLE_EVENTS_AND_REPORTS;
import org.llrp.ltk.generated.messages.ENABLE_ROSPEC;
import org.llrp.ltk.generated.messages.ENABLE_ROSPEC_RESPONSE;
import org.llrp.ltk.generated.messages.GET_READER_CAPABILITIES;
import org.llrp.ltk.generated.messages.GET_READER_CAPABILITIES_RESPONSE;
import org.llrp.ltk.generated.messages.GET_REPORT;
import org.llrp.ltk.generated.messages.RO_ACCESS_REPORT;
import org.llrp.ltk.generated.messages.START_ROSPEC;
import org.llrp.ltk.generated.messages.START_ROSPEC_RESPONSE;
import org.llrp.ltk.generated.parameters.AISpec;
import org.llrp.ltk.generated.parameters.AISpecStopTrigger;
import org.llrp.ltk.generated.parameters.AccessCommand;
import org.llrp.ltk.generated.parameters.AccessReportSpec;
import org.llrp.ltk.generated.parameters.AccessSpec;
import org.llrp.ltk.generated.parameters.AccessSpecStopTrigger;
import org.llrp.ltk.generated.parameters.AntennaConfiguration;
import org.llrp.ltk.generated.parameters.C1G2Read;
import org.llrp.ltk.generated.parameters.C1G2TagSpec;
import org.llrp.ltk.generated.parameters.C1G2TargetTag;
import org.llrp.ltk.generated.parameters.InventoryParameterSpec;
import org.llrp.ltk.generated.parameters.RFTransmitter;
import org.llrp.ltk.generated.parameters.ROBoundarySpec;
import org.llrp.ltk.generated.parameters.ROReportSpec;
import org.llrp.ltk.generated.parameters.ROSpec;
import org.llrp.ltk.generated.parameters.ROSpecStartTrigger;
import org.llrp.ltk.generated.parameters.ROSpecStopTrigger;
import org.llrp.ltk.generated.parameters.TagReportContentSelector;
import org.llrp.ltk.generated.parameters.TransmitPowerLevelTableEntry;
import org.llrp.ltk.net.LLRPConnectionAttemptFailedException;
import org.llrp.ltk.net.LLRPConnector;
import org.llrp.ltk.net.LLRPEndpoint;
import org.llrp.ltk.types.Bit;
import org.llrp.ltk.types.BitArray_HEX;
import org.llrp.ltk.types.LLRPMessage;
import org.llrp.ltk.types.TwoBitField;
import org.llrp.ltk.types.UnsignedByte;
import org.llrp.ltk.types.UnsignedInteger;
import org.llrp.ltk.types.UnsignedShort;
import org.llrp.ltk.types.UnsignedShortArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * This class is the result of information found on
 * http://learn.impinj.com/articles/en_US/RFID/Reading-and-Writing-User-Memory-with-the-Java-LTK/
 */
public final class LLRPReader {

    private static final Logger LOG = LoggerFactory.getLogger(LLRPReader.class);
    private LLRPConnector reader;
    private static final int TIMEOUT_MS = 10000;
    private static final long CONNECT_TIMEOUT = 10000L;
    private static final int ROSPEC_ID = 123;
    private static final int ACCESSSPEC_ID = 444;
    private static final int OPSPEC_ID = 445;
    private final LLRPEndpoint llrpEndpoint;

    /**
     * Create a new reader, relay LLRP messages to
     * the given messageConsumer, and errors to the
     * given errorConsumer.
     */
    public LLRPReader(LLRPEndpoint llrpEndpoint) {
        this.llrpEndpoint = llrpEndpoint;
    }

    public ROSpec buildROSpec() {

        ROSpec roSpec = new ROSpec();
        roSpec.setPriority(new UnsignedByte(0));
        roSpec.setCurrentState(new ROSpecState(ROSpecState.Disabled));
        roSpec.setROSpecID(new UnsignedInteger(ROSPEC_ID));


        ROBoundarySpec roBoundarySpec = new ROBoundarySpec();
        ROSpecStartTrigger startTrig = new ROSpecStartTrigger();
        startTrig.setROSpecStartTriggerType(new ROSpecStartTriggerType(ROSpecStartTriggerType.Null));

        ROSpecStopTrigger stopTrig = new ROSpecStopTrigger();
        stopTrig.setDurationTriggerValue(new UnsignedInteger(0));
        stopTrig.setROSpecStopTriggerType(new ROSpecStopTriggerType(ROSpecStopTriggerType.Null));

        roBoundarySpec.setROSpecStartTrigger(startTrig);
        roBoundarySpec.setROSpecStopTrigger(stopTrig);

        roSpec.setROBoundarySpec(roBoundarySpec);

        AISpec aispec = new AISpec();

        AISpecStopTrigger aiStopTrigger = new AISpecStopTrigger();
        aiStopTrigger.setAISpecStopTriggerType(new AISpecStopTriggerType(AISpecStopTriggerType.Null));
        aiStopTrigger.setDurationTrigger(new UnsignedInteger(0));
        aispec.setAISpecStopTrigger(aiStopTrigger);

        UnsignedShortArray antennaIDs = new UnsignedShortArray();
        antennaIDs.add(new UnsignedShort(0));
        aispec.setAntennaIDs(antennaIDs);

        InventoryParameterSpec inventoryParam = new InventoryParameterSpec();
        inventoryParam.setProtocolID(new AirProtocols(AirProtocols.EPCGlobalClass1Gen2));
        inventoryParam.setInventoryParameterSpecID(new UnsignedShort(1));

        roSpec.addToSpecParameterList(aispec);

        AntennaConfiguration antConfig = new AntennaConfiguration();
        antConfig.setAntennaID(new UnsignedShort(0));

        RFTransmitter tx = new RFTransmitter();
        tx.setTransmitPower(new UnsignedShort(87)); // TODO: Is this the max?
        tx.setChannelIndex(new UnsignedShort(1));
        tx.setHopTableID(new UnsignedShort(1));
        antConfig.setRFTransmitter(tx);

        inventoryParam.addToAntennaConfigurationList(antConfig);
        aispec.addToInventoryParameterSpecList(inventoryParam);

        // Specify what type of tag reports we want
        // to receive and when we want to receive them.
        ROReportSpec roReportSpec = new ROReportSpec();
        // Receive a report every time a tag is read.
        roReportSpec.setROReportTrigger(new ROReportTriggerType(
                ROReportTriggerType.Upon_N_Tags_Or_End_Of_ROSpec));
        roReportSpec.setN(new UnsignedShort(1));
        TagReportContentSelector reportContent = new TagReportContentSelector();
        // Select which fields we want in the report.
        reportContent.setEnableAccessSpecID(new Bit(1));
        reportContent.setEnableAntennaID(new Bit(1));
        reportContent.setEnableChannelIndex(new Bit(1));
        reportContent.setEnableFirstSeenTimestamp(new Bit(1));
        reportContent.setEnableInventoryParameterSpecID(new Bit(1));
        reportContent.setEnableLastSeenTimestamp(new Bit(1));
        reportContent.setEnablePeakRSSI(new Bit(1));
        reportContent.setEnableROSpecID(new Bit(1));
        reportContent.setEnableSpecIndex(new Bit(1));
        reportContent.setEnableTagSeenCount(new Bit(1));
        roReportSpec.setTagReportContentSelector(reportContent);
        roSpec.setROReportSpec(roReportSpec);

        return roSpec;
    }

    public void enableReports() throws TimeoutException {
        try {
            ENABLE_EVENTS_AND_REPORTS reports = new ENABLE_EVENTS_AND_REPORTS();
            reader.send(reports);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void getReports() throws TimeoutException {
        GET_REPORT reports = new GET_REPORT();
        reader.send(reports);

    }

    public List<TransmitPowerLevelTableEntry> readTransmitPowerEntries() throws LLRPException {
        GET_READER_CAPABILITIES_RESPONSE response;

        try {
            GET_READER_CAPABILITIES getReaderCaps = new GET_READER_CAPABILITIES();
            getReaderCaps.setRequestedData(new GetReaderCapabilitiesRequestedData(GetReaderCapabilitiesRequestedData.All));
            response = (GET_READER_CAPABILITIES_RESPONSE) reader.transact(getReaderCaps, TIMEOUT_MS);

            StatusCode status = response.getLLRPStatus().getStatusCode();
            if (status.equals(new StatusCode(StatusCode.M_Success))) {
                return response.getRegulatoryCapabilities().getUHFBandCapabilities().getTransmitPowerLevelTableEntryList();
            } else {
                LOG.error("Error reading capabilities. Status code: {}", status.toString());
                throw new LLRPException();
            }
        } catch (TimeoutException e) {
            LOG.error("Timeout when adding reading capabilities.", e);
            throw new LLRPException(e);
        }
    }

    // Add the ROSpec to the reader.
    public void addROSpec() throws LLRPException {
        ADD_ROSPEC_RESPONSE response;

        ROSpec roSpec = buildROSpec();
        LOG.info("Adding the ROSpec");
        try {
            ADD_ROSPEC roSpecMsg = new ADD_ROSPEC();
            roSpecMsg.setROSpec(roSpec);
            response = (ADD_ROSPEC_RESPONSE) reader.transact(roSpecMsg, TIMEOUT_MS);
            LOG.info("Adding ROSpec response: {}", response.toXMLString());

            // Check if the we successfully added the ROSpec.
            StatusCode status = response.getLLRPStatus().getStatusCode();
            if (status.equals(new StatusCode(StatusCode.M_Success))) {
                LOG.info("Successfully added ROSpec.");
            } else {
                // TODO: use the status code, Luke!
                LOG.error("Error adding ROSpec. Status code: {}", status.toString());
                throw new LLRPException();
            }
        } catch (TimeoutException e) {
            LOG.error("Timeout when adding ROSpec.", e);
            throw new LLRPException(e);
        } catch (InvalidLLRPMessageException e) {
            LOG.error("Formed invalid ADD_ROSPEC message.", e);
            throw new LLRPException(e);
        }
    }

    // Enable the ROSpec.
    public void enableROSpec() throws LLRPException {
        ENABLE_ROSPEC_RESPONSE response;

        LOG.info("Enabling the ROSpec.");
        ENABLE_ROSPEC enable = new ENABLE_ROSPEC();
        enable.setROSpecID(new UnsignedInteger(ROSPEC_ID));
        try {
            response = (ENABLE_ROSPEC_RESPONSE) reader.transact(enable, TIMEOUT_MS);
            LOG.info("ROSpec enable response: {}", response.toXMLString());
        } catch (TimeoutException e) {
            LOG.error("Timeout when enabling ROSpec.", e);
            throw new LLRPException(e);
        } catch (InvalidLLRPMessageException e) {
            LOG.error("Formed invalid ENABLE_ROSPEC message.", e);
            throw new LLRPException(e);
        }
    }

    // Start the ROSpec.
    public void startROSpec() throws LLRPException {
        START_ROSPEC_RESPONSE response;
        LOG.info("Starting the ROSpec.");
        START_ROSPEC start = new START_ROSPEC();
        start.setROSpecID(new UnsignedInteger(ROSPEC_ID));
        try {
            response = (START_ROSPEC_RESPONSE) reader.transact(start, TIMEOUT_MS);
            LOG.info("Start ROSpec response: {}", response.toXMLString());
        } catch (TimeoutException e) {
            LOG.error("Timeout when starting ROSpec.", e);
            throw new LLRPException(e);
        } catch (InvalidLLRPMessageException e) {
            LOG.error("Formed invalid START_ROSPEC message.", e);
            throw new LLRPException(e);
        }
    }

    // Delete all ROSpecs from the reader.
    public void deleteROSpecs() throws LLRPException {
        DELETE_ROSPEC_RESPONSE response;

        LOG.info("Deleting all ROSpecs.");
        DELETE_ROSPEC del = new DELETE_ROSPEC();
        // Use zero as the ROSpec ID.
        // This means delete all ROSpecs.
        del.setROSpecID(new UnsignedInteger(0));
        try {
            response = (DELETE_ROSPEC_RESPONSE) reader.transact(del, TIMEOUT_MS);
            LOG.info("Delete ROSpec response: {}", response.toXMLString());
        } catch (TimeoutException e) {
            LOG.error("Timeout when deleting ROSpec.", e);
            throw new LLRPException(e);
        } catch (InvalidLLRPMessageException e) {
            LOG.error("Formed invalid DELETE_ROSPEC message.", e);
            throw new LLRPException(e);
        }
    }

    public AccessReportSpec addAccessReport() {
        AccessReportSpec accessReportSpec = new AccessReportSpec();
        accessReportSpec.setAccessReportTrigger(new AccessReportTriggerType(AccessReportTriggerType.End_Of_AccessSpec));

        return accessReportSpec;

    }

    public AccessSpecStopTrigger addAccessSpecStopTrigger() {
        AccessSpecStopTrigger stopTrig = new AccessSpecStopTrigger();
        stopTrig.setOperationCountValue(new UnsignedShort(0));
        stopTrig.setAccessSpecStopTrigger(new AccessSpecStopTriggerType(AccessSpecStopTriggerType.Null));
        return stopTrig;
    }

    private List<AccessCommandOpSpec> generateOpSpecList() {

        // A list to hold the op specs for this access command.
        List<AccessCommandOpSpec> opSpecList =
                new ArrayList<>();

        // Set default opspec which for eventcycle of accessspec 3.
        C1G2Read opSpec1 = new C1G2Read();
        // Set the OpSpecID to a unique number.
        opSpec1.setOpSpecID(new UnsignedShort(999));
        opSpec1.setAccessPassword(new UnsignedInteger(0));

        // We'll read from user memory (bank 3).
        TwoBitField opMemBank = new TwoBitField("3");
        opSpec1.setMB(opMemBank);

        // We'll read from the base of this memory bank (0x00).
        opSpec1.setWordPointer(new UnsignedShort(0));
        // Read two words.
        opSpec1.setWordCount(new UnsignedShort(0));

        opSpecList.add(opSpec1);


        /**************/
        // Set default opspec which for eventcycle of accessspec 2 .
        C1G2Read opSpec2 = new C1G2Read();
        // Set the OpSpecID to a unique number.
        opSpec2.setOpSpecID(new UnsignedShort(998));
        opSpec2.setAccessPassword(new UnsignedInteger(0));

        // We'll read from user memory (bank 2).
        TwoBitField opMemBank2 = new TwoBitField("2");
        opSpec2.setMB(opMemBank2);

        // We'll read from the base of this memory bank (0x00).
        opSpec2.setWordPointer(new UnsignedShort(0));
        // Read two words.
        opSpec2.setWordCount(new UnsignedShort(0));
        opSpecList.add(opSpec2);
        /*************/

        return opSpecList;
    }

    public AccessCommand addAccessCommand() {
        AccessCommand accessCommand = new AccessCommand();
        accessCommand.setAccessCommandOpSpecList(generateOpSpecList());

        C1G2TagSpec tagSpec = new C1G2TagSpec();
        C1G2TargetTag targetTag = new C1G2TargetTag();
        targetTag.setMatch(new Bit(1));

        TwoBitField tbw = new TwoBitField("2");
        targetTag.setMB(tbw);
        targetTag.setPointer(new UnsignedShort(0));


        BitArray_HEX tagMask = new BitArray_HEX("00");
        targetTag.setTagMask(tagMask);

        BitArray_HEX tagData = new BitArray_HEX("00");
        targetTag.setTagData(tagData);

        List<C1G2TargetTag> targetTagList = new ArrayList();
        targetTagList.add(targetTag);
        tagSpec.setC1G2TargetTagList(targetTagList);

        accessCommand.setAirProtocolTagSpec(tagSpec);


      /*  C1G2Read c1G2Read = new C1G2Read();
        c1G2Read.setOpSpecID(new UnsignedShort(OPSPEC_ID));
        c1G2Read.setAccessPassword(new UnsignedInteger(1));

        tbw.set(0);
        c1G2Read.setMB(tbw);
        c1G2Read.setWordPointer(new UnsignedShort(0));
        c1G2Read.setWordCount(new UnsignedShort(0));
*/
        return accessCommand;
    }

    public void addAccessSpec() throws LLRPException, TimeoutException, InvalidLLRPMessageException {

        DELETE_ACCESSSPEC del = new DELETE_ACCESSSPEC();
        del.setAccessSpecID(new UnsignedInteger(ACCESSSPEC_ID));


        ////////////////
        AccessSpec accessSpec = new AccessSpec();
        accessSpec.setROSpecID(new UnsignedInteger(ROSPEC_ID));
        accessSpec.setAccessSpecID(new UnsignedInteger(ACCESSSPEC_ID));
        accessSpec.setAccessReportSpec(addAccessReport());
        accessSpec.setAccessSpecStopTrigger(addAccessSpecStopTrigger());
        accessSpec.setAntennaID(new UnsignedShort(0));
        accessSpec.setAccessCommand(addAccessCommand());
        accessSpec.setProtocolID(new AirProtocols(AirProtocols.EPCGlobalClass1Gen2));

        accessSpec.setCurrentState(new AccessSpecState(AccessSpecState.Disabled));

        ADD_ACCESSSPEC accessspec = new ADD_ACCESSSPEC();
        accessspec.setAccessSpec(accessSpec);

        LOG.info("Add access spec message :", accessspec.toXMLString());

        ////////////////
        ENABLE_ACCESSSPEC enable_accessspec = new ENABLE_ACCESSSPEC();
        enable_accessspec.setAccessSpecID(accessSpec.getAccessSpecID());


    }

    public void getSupportedVersion() throws LLRPException, TimeoutException {


        GET_READER_CAPABILITIES readerCapabilities = new GET_READER_CAPABILITIES();
        readerCapabilities.setRequestedData(new GetReaderCapabilitiesRequestedData(GetReaderCapabilitiesRequestedData.All));
        GET_READER_CAPABILITIES_RESPONSE response = (GET_READER_CAPABILITIES_RESPONSE) reader.transact(readerCapabilities, TIMEOUT_MS);

        try {
            LOG.info("XXXXXXXXXXXXXXX" + response.toXMLString());
        } catch (InvalidLLRPMessageException e) {
            e.printStackTrace();
        }

    }

    // Connect to the reader
    public void connect() throws LLRPException {
        // Create the reader object.

        // reader = new LLRPConnector(this, "192.168.0.111", 5084);

        reader = new LLRPConnector(llrpEndpoint, "localhost", 55555);


        // Try connecting to the reader.
        try {
            LOG.info("Connecting to the reader.");
            // NOTE: The timeout is a lot longer
            reader.connect(CONNECT_TIMEOUT);
        } catch (LLRPConnectionAttemptFailedException e1) {
            LOG.error("Error connecting to the reader", e1);
            throw new LLRPException(e1);
        }
    }

    // Disconnect from the reader
    public void disconnect() {
        reader.disconnect();
    }

    public boolean run() {
        try {
            connect();
            //  getSupportedVersion();
            deleteROSpecs();
            addROSpec();
            enableROSpec();
            startROSpec();
            addAccessSpec();
            enableReports();
            getReports();
            return true;
        } catch (LLRPException e) {
            LOG.error("error", e);
            return false;
        } catch (TimeoutException e) {
            e.printStackTrace();
            return false;
        } catch (InvalidLLRPMessageException e) {
            e.printStackTrace();
            return false;
        }
    }
}
