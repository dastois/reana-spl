package Modeling;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.DOMException;

import fdtmc.FDTMC;
import fdtmc.State;
import Parsing.Node;
import Parsing.ActivityDiagrams.ADReader;
import Parsing.ActivityDiagrams.Activity;
import Parsing.ActivityDiagrams.ActivityType;
import Parsing.ActivityDiagrams.Edge;
import Parsing.Exceptions.InvalidNodeClassException;
import Parsing.Exceptions.InvalidNodeType;
import Parsing.Exceptions.InvalidNumberOfOperandsException;
import Parsing.Exceptions.InvalidTagException;
import Parsing.Exceptions.UnsupportedFragmentTypeException;
import Parsing.SequenceDiagrams.Fragment;
import Parsing.SequenceDiagrams.Message;
import Parsing.SequenceDiagrams.MessageType;
import Parsing.SequenceDiagrams.Operand;
import Parsing.SequenceDiagrams.SDReader;
import Transformation.Transformer;

public class DiagramAPI {
	// Attributes
	
		private final File xmlFile;
		private ArrayList<SDReader> sdParsers;
		private ADReader adParser;
		private HashMap<String, Fragment> sdByID;
		private Transformer transformer;
		
		//private HashMap<String, FDTMC> fdtmcByName;
		//private HashMap<String, State> stateByActID;
	
	// Constructors

		public DiagramAPI(File xmlFile) throws DOMException, UnsupportedFragmentTypeException, InvalidTagException {
			this.xmlFile = xmlFile;
			adParser = null;
			sdParsers = new ArrayList<SDReader>();
			sdByID = new HashMap<String, Fragment>();
			transformer = new Transformer();

			initialize();
		}
		
	// Relevant public methods


		/**
		 * Triggers the applicable transformations, either AD or SD based
		 * @throws InvalidNumberOfOperandsException 
		 * @throws InvalidNodeClassException 
		 */
		public void transform() throws InvalidNumberOfOperandsException, InvalidNodeClassException, InvalidNodeType {
			for (ADReader adParser : this.adParsers) {
				transformer.transformSingleAD(adParser);
			}

			for (SDReader sdParser : this.sdParsers) {
				transformer.transformSingleSD(sdParser.getSD());
			}
		}
		
		public void measureSizeModel (FDTMC fdtmc) {
			transformer.measureSizeModel(fdtmc);
		}
		
		public void printNumberOfCalls (String name) {
			transformer.printNumberOfCalls(name);
		}

	// Relevant private methods

		/**
		 * Initializes the model transformation activities,
		 * starting from parsing the XMI file and
		 * then applying the transformation functions
		 * @throws InvalidTagException
		 * @throws UnsupportedFragmentTypeException
		 * @throws InvalidGuardException
		 * @throws DOMException
		 */
		private void initialize() throws UnsupportedFragmentTypeException, InvalidTagException, DOMException {
		    ADReader adParser = new ADReader(this.xmlFile, 0);
		    adParser.retrieveActivities();
		    this.adParser = adParser;

		    boolean hasNext = false;
		    int index = 0;
		    do {
		        SDReader sdParser = new SDReader(this.xmlFile, index);
		        sdParser.traceDiagram();
		        sdByID.put(sdParser.getSD().getId(), sdParser.getSD());
		        this.sdParsers.add(sdParser);
		        hasNext = sdParser.hasNext();
		        index++;
		    } while (hasNext);
		    linkSdToActivity(this.adParser);
		}

		/**
		 * Links activities of an AD to their respective SD
		 * @param ad
		 */
		private void linkSdToActivity(ADReader ad) {
			for (Activity a : ad.getActivities()) {
				if (a.getSdID() != null) {
					a.setSd(sdByID.get(a.getSdID()));
				}
			}
		}
		
	// Getters and Setters

		public HashMap<String, FDTMC> getFdtmcByName() {
			return transformer.getFdtmcByName();
		}

		public ArrayList<SDReader> getSdParsers() {
			return sdParsers;
		}

		public ADReader getAdParser() {
			return adParser;
		}

}
