package com.test.land;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.QueryByChaincodeRequest;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;

/**
 * 
 * @author pradeep
 *
 */
public class LandRecordSDKMain {

	public static void main(String[] args) throws Exception {

		HFCAClient caClient = HFCAClient.createNewInstance("http://localhost:7054", null);
		caClient.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());

		UserImpl admin = getAdmin(caClient);
		System.out.println(admin);

		HFClient client = HFClient.createNewInstance();
		client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
		client.setUserContext(admin);

		Channel channel = client.newChannel("mychannel")
				.addPeer(client.newPeer("peer0.org1.example.com", "grpc://localhost:7051"))
				.addEventHub(client.newEventHub("eventhub01", "grpc://localhost:7053"))
				.addOrderer(client.newOrderer("orderer.example.com", "grpc://localhost:7050"));
		channel.initialize();

		channel = client.getChannel("mychannel");

		QueryByChaincodeRequest queryByChaincodeRequest = null;
		ChaincodeID landChainCodeID = null;
		Collection<ProposalResponse> proposalResponses = null;
		TransactionProposalRequest transactionProposalRequest = null;
		List<ProposalResponse> invalid = null;
		BlockEvent.TransactionEvent event = null;

		System.out.println("\nCalling queryAllLands()");
		queryByChaincodeRequest = client.newQueryProposalRequest();
		landChainCodeID = ChaincodeID.newBuilder().setName("land").build();
		queryByChaincodeRequest.setChaincodeID(landChainCodeID);
		queryByChaincodeRequest.setFcn("queryAllLands");
		proposalResponses = channel.queryByChaincode(queryByChaincodeRequest);
		for (ProposalResponse pres : proposalResponses) {
			String stringResponse = new String(pres.getChaincodeActionResponsePayload());
			System.out.println(stringResponse);
		}
		System.out.println("queryAllLands() completed\n\n");

		System.out.println("Calling createLand()");
		transactionProposalRequest = client.newTransactionProposalRequest();
		landChainCodeID = ChaincodeID.newBuilder().setName("land").build();
		transactionProposalRequest.setChaincodeID(landChainCodeID);
		transactionProposalRequest.setFcn("createLand");
		transactionProposalRequest
				.setArgs(new String[] { "PID" + proposalResponses.size() + 1, "Bangalore", "AGRI", "CREATED", "RAJ" });
		proposalResponses = channel.sendTransactionProposal(transactionProposalRequest);
		invalid = proposalResponses.stream().filter(r -> r.isInvalid()).collect(Collectors.toList());
		if (!invalid.isEmpty()) {
			invalid.forEach(response -> {
				System.out.println(response.getMessage());
			});
			throw new RuntimeException("invalid response(s) found");
		}
		event = channel.sendTransaction(proposalResponses).get(60, TimeUnit.SECONDS);
		if (event.isValid()) {
			System.out.println("Transacion tx: " + event.getTransactionID() + " is completed.");
		} else {
			System.out.println("Transaction tx: " + event.getTransactionID() + " is invalid.");
		}
		System.out.println("createLand()completed\n\n");

		System.out.println("Calling queryLandWithPID(PID3)");
		queryByChaincodeRequest = client.newQueryProposalRequest();
		landChainCodeID = ChaincodeID.newBuilder().setName("land").build();
		queryByChaincodeRequest.setChaincodeID(landChainCodeID);
		queryByChaincodeRequest.setFcn("queryLedger");
		queryByChaincodeRequest.setArgs(new String[] { "PID3" });
		proposalResponses = channel.queryByChaincode(queryByChaincodeRequest);
		for (ProposalResponse pres : proposalResponses) {
			String stringResponse = new String(pres.getChaincodeActionResponsePayload());
			System.out.println(stringResponse);
		}
		System.out.println("queryLandWithPID(PID3) completed\n\n");

		System.out.println("Calling changeLandOwner(PID3)");
		transactionProposalRequest = client.newTransactionProposalRequest();
		landChainCodeID = ChaincodeID.newBuilder().setName("land").build();
		transactionProposalRequest.setChaincodeID(landChainCodeID);
		transactionProposalRequest.setFcn("changeLandOwner");
		transactionProposalRequest.setArgs(new String[] { "PID3", "KJ" });
		proposalResponses = channel.sendTransactionProposal(transactionProposalRequest);
		invalid = proposalResponses.stream().filter(r -> r.isInvalid()).collect(Collectors.toList());
		if (!invalid.isEmpty()) {
			invalid.forEach(response -> {
				System.out.println(response.getMessage());
			});
			throw new RuntimeException("invalid response(s) found");
		}
		event = channel.sendTransaction(proposalResponses).get(60, TimeUnit.SECONDS);
		if (event.isValid()) {
			System.out.println("Transacion tx: " + event.getTransactionID() + " is completed.");
		} else {
			System.out.println("Transaction tx: " + event.getTransactionID() + " is invalid.");
		}
		System.out.println("Calling changeLandOwner(PID3)completed\n\n");

		System.out.println("Calling changeLandStatus(PID3)");
		transactionProposalRequest = client.newTransactionProposalRequest();
		landChainCodeID = ChaincodeID.newBuilder().setName("land").build();
		transactionProposalRequest.setChaincodeID(landChainCodeID);
		transactionProposalRequest.setFcn("changeLandStatus");
		transactionProposalRequest.setArgs(new String[] { "PID3", "FOR_SALE" });
		proposalResponses = channel.sendTransactionProposal(transactionProposalRequest);
		invalid = proposalResponses.stream().filter(r -> r.isInvalid()).collect(Collectors.toList());
		if (!invalid.isEmpty()) {
			invalid.forEach(response -> {
				System.out.println(response.getMessage());
			});
			throw new RuntimeException("invalid response(s) found");
		}
		event = channel.sendTransaction(proposalResponses).get(60, TimeUnit.SECONDS);
		if (event.isValid()) {
			System.out.println("Transacion tx: " + event.getTransactionID() + " is completed.");
		} else {
			System.out.println("Transaction tx: " + event.getTransactionID() + " is invalid.");
		}
		System.out.println("Calling changeLandStatus(PID3)completed\n\n");

		System.out.println("Calling queryLandWithPID(PID3)");
		queryByChaincodeRequest = client.newQueryProposalRequest();
		landChainCodeID = ChaincodeID.newBuilder().setName("land").build();
		queryByChaincodeRequest.setChaincodeID(landChainCodeID);
		queryByChaincodeRequest.setFcn("queryLedger");
		queryByChaincodeRequest.setArgs(new String[] { "PID3" });
		proposalResponses = channel.queryByChaincode(queryByChaincodeRequest);
		for (ProposalResponse pres : proposalResponses) {
			String stringResponse = new String(pres.getChaincodeActionResponsePayload());
			System.out.println(stringResponse);
		}
		System.out.println("queryLandWithPID(PID3) completed\n\n");

		System.out.println("Calling deleteLand(PID1)");
		transactionProposalRequest = client.newTransactionProposalRequest();
		landChainCodeID = ChaincodeID.newBuilder().setName("land").build();
		transactionProposalRequest.setChaincodeID(landChainCodeID);
		transactionProposalRequest.setFcn("deleteLand");
		transactionProposalRequest.setArgs(new String[] { "PID1" });
		proposalResponses = channel.sendTransactionProposal(transactionProposalRequest);
		invalid = proposalResponses.stream().filter(r -> r.isInvalid()).collect(Collectors.toList());
		if (!invalid.isEmpty()) {
			invalid.forEach(response -> {
				System.out.println(response.getMessage());
			});
			throw new RuntimeException("invalid response(s) found");
		}
		event = channel.sendTransaction(proposalResponses).get(60, TimeUnit.SECONDS);
		if (event.isValid()) {
			System.out.println("Transacion tx: " + event.getTransactionID() + " is completed.");
		} else {
			System.out.println("Transaction tx: " + event.getTransactionID() + " is invalid.");
		}
		System.out.println("Calling deleteLand(PID1)completed\n\n");

		System.out.println("\nCalling queryAllLands()");
		queryByChaincodeRequest = client.newQueryProposalRequest();
		landChainCodeID = ChaincodeID.newBuilder().setName("land").build();
		queryByChaincodeRequest.setChaincodeID(landChainCodeID);
		queryByChaincodeRequest.setFcn("queryAllLands");
		proposalResponses = channel.queryByChaincode(queryByChaincodeRequest);
		for (ProposalResponse pres : proposalResponses) {
			String stringResponse = new String(pres.getChaincodeActionResponsePayload());
			System.out.println(stringResponse);
		}
		System.out.println("queryAllLands() completed\n\n");

	}

	public static UserImpl getAdmin(HFCAClient caClient) throws Exception {
		UserImpl admin = tryDeserialize("admin");
		if (admin == null) {
			Enrollment adminEnrollment = caClient.enroll("admin", "adminpw");
			admin = new UserImpl("admin", "org1", "Org1MSP", adminEnrollment);
			serialize(admin);
		}
		return admin;
	}

	public static void serialize(UserImpl appUser) throws IOException {
		try (ObjectOutputStream oos = new ObjectOutputStream(
				Files.newOutputStream(Paths.get(appUser.getName() + ".jso")))) {
			oos.writeObject(appUser);
		}
	}

	public static UserImpl tryDeserialize(String name) throws Exception {
		if (Files.exists(Paths.get(name + ".jso"))) {
			return deserialize(name);
		}
		return null;
	}

	public static UserImpl deserialize(String name) throws Exception {
		try (ObjectInputStream decoder = new ObjectInputStream(Files.newInputStream(Paths.get(name + ".jso")))) {
			return (UserImpl) decoder.readObject();
		}
	}
}

class UserImpl implements User, Serializable {

	private String name;
	private Set<String> roles;
	private String account;
	private String affiliation;
	private Enrollment enrollment;
	private String mspId;

	public UserImpl() {
		// no-arg constructor
	}

	public UserImpl(String name, String affiliation, String mspId, Enrollment enrollment) {
		this.name = name;
		this.affiliation = affiliation;
		this.enrollment = enrollment;
		this.mspId = mspId;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public Set<String> getRoles() {
		return roles;
	}

	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}

	@Override
	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	@Override
	public String getAffiliation() {
		return affiliation;
	}

	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
	}

	@Override
	public Enrollment getEnrollment() {
		return enrollment;
	}

	public void setEnrollment(Enrollment enrollment) {
		this.enrollment = enrollment;
	}

	@Override
	public String getMspId() {
		return mspId;
	}

	public void setMspId(String mspId) {
		this.mspId = mspId;
	}

	@Override
	public String toString() {
		return "AppUser{" + "name='" + name + '\'' + "\n, roles=" + roles + "\n, account='" + account + '\''
				+ "\n, affiliation='" + affiliation + '\'' + "\n, enrollment=" + enrollment + "\n, mspId='" + mspId
				+ '\'' + '}';
	}
}
