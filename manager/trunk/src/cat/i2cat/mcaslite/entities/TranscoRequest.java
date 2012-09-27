package cat.i2cat.mcaslite.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cat.i2cat.mcaslite.exceptions.MCASException;

public class TranscoRequest {

	public enum State {
		CREATED(1, "CREATED"){
			public State next(){return M_QUEUED;}
		}, 
		M_QUEUED(2, "M_QUEUED"){
			public State next(){return M_PROCESS;}
		}, 
		M_PROCESS(3, "M_PROCESS"){
			public State next(){return T_QUEUED;}
		}, 
		T_QUEUED(4, "T_QUEUED"){
			public State next(){return T_PROCESS;}
		}, 
		T_PROCESS(5, "T_PROCESS"){
			public State next(){return T_TRANSCODED;}
		}, 
		T_TRANSCODED(6, "T_TRANSCODED"){
			public State next(){return MOVING;}
		}, 
		MOVING(7, "MOVING"){
			public State next(){return DONE;}
		},
		DONE(8, "DONE"){
			public State next() throws MCASException{throw new MCASException();}
		},
		PARTIAL_ERROR(101, "PARTIAL_ERROR"){
			public State next() throws MCASException{throw new MCASException();}
		},
		ERROR(100, "ERROR"){
			public State next() throws MCASException{throw new MCASException();}
		};

		private final int id;
		private final String name;
		
		public abstract State next() throws MCASException;
		
		State(int id, String name){
			this.id = id;
			this.name = name;
		}
		
		public int getId(){
			return this.id;
		}
		
		public String getName(){
			return this.name;
		}

	}

	private String src;
	private String dst;
	private String config;
	private String usr;
	private UUID id = UUID.randomUUID();
	private State state = State.CREATED;
	private int numOutputs;
	private List<String> transcodedUris = new ArrayList<String>();
			
	public void addTrancodedUri(String uri){
		transcodedUris.add(uri);
	}
	
	public boolean isTranscodedUriEmpty(){
		return transcodedUris.isEmpty();
	}
	
	public int getNumOutputs() {
		return numOutputs;
	}

	public void setNumOutputs(int numOutputs) {
		this.numOutputs = numOutputs;
	}

	public void increaseState() throws MCASException {
		state = state.next();
	}
	
	public void setError() {
		state = State.ERROR;
	}
	
	public void setPartialError() {
		state = State.PARTIAL_ERROR;
	}

	public State getState() {
		return state;
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public String getDst() {
		return dst;
	}

	public void setDst(String dst) {
		this.dst = dst;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public String getUsr() {
		return usr;
	}

	public void setUsr(String usr) {
		this.usr = usr;
	}

	public UUID getId() {
		return id;
	}

	public String getIdStr() {
		return id.toString();
	}

	public String getIdString() {
		return id.toString();
	}
	
	public static TranscoRequest getEqualRequest(UUID id){
		TranscoRequest req = new TranscoRequest();
		req.id = id;
		return req;
	}

	@Override 
	public boolean equals(Object o){
		TranscoRequest req = (TranscoRequest) o;
		return this.id.equals(req.getId());
	}

}
