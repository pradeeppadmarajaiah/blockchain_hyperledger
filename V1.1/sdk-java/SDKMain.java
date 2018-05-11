package com.bit.i.know;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.QueryByChaincodeRequest;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;

public class SDKMain {

	public static void main(String[] args) {

		try {

			// CA Client instantiation
			CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
			HFCAClient hfcaClient = HFCAClient.createNewInstance("grpc://localhost:7054", null);
			hfcaClient.setCryptoSuite(cryptoSuite);

			// Enroll Admin
			Enrollment enrollment = hfcaClient.enroll("admin", "adminpw");
			UserImpl admin = new UserImpl("admin", "org1", "Org1MSP", enrollment);
			// Store it

			// Register Enroll New user
			RegistrationRequest memberRequest = new RegistrationRequest("hfuser", "org1");
			// member is registered by admin
			String secret = hfcaClient.register(memberRequest, admin);
			Enrollment memberEnrollment = hfcaClient.enroll("hfuser", secret);
			UserImpl hfuser = new UserImpl("hfuser", "org1", "Org1MSP", memberEnrollment);
			// Store it

			CryptoSuite farbricCryptoSuite = CryptoSuite.Factory.getCryptoSuite();
			HFClient fabricClient = HFClient.createNewInstance();
			fabricClient.setCryptoSuite(farbricCryptoSuite);
			fabricClient.setUserContext(admin);

			Channel channel = fabricClient.newChannel("mycc");
			channel.addPeer(fabricClient.newPeer("peer", "grpc://localhost:7051"));
			channel.addOrderer(fabricClient.newOrderer("orderer", "grpc://localhost:7050"));
			channel.addEventHub(fabricClient.newEventHub("eventhub", "grpc://localhost:7053"));
			channel.initialize();

			
			// Query All Lands
			QueryByChaincodeRequest queryByChaincodeRequest = fabricClient.newQueryProposalRequest();
			queryByChaincodeRequest.setChaincodeID(ChaincodeID.newBuilder().setName("landRecord").build());
			queryByChaincodeRequest.setFcn("queryAllLands");

			Collection<ProposalResponse> responses = channel.queryByChaincode(queryByChaincodeRequest);
			for (ProposalResponse pres : responses) {
				System.out.println(pres.getChaincodeActionResponsePayload());
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	}

	public UserImpl(String name, String affiliation, String mspId, Enrollment enrollment) {
		this.name = name;
		this.affiliation = affiliation;
		this.enrollment = enrollment;
		this.mspId = mspId;
	}

	/**
	 * @return the name
	 */
	public final String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public final void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the roles
	 */
	public final Set<String> getRoles() {
		return roles;
	}

	/**
	 * @param roles
	 *            the roles to set
	 */
	public final void setRoles(Set<String> roles) {
		this.roles = roles;
	}

	/**
	 * @return the account
	 */
	public final String getAccount() {
		return account;
	}

	/**
	 * @param account
	 *            the account to set
	 */
	public final void setAccount(String account) {
		this.account = account;
	}

	/**
	 * @return the affiliation
	 */
	public final String getAffiliation() {
		return affiliation;
	}

	/**
	 * @param affiliation
	 *            the affiliation to set
	 */
	public final void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
	}

	/**
	 * @return the enrollment
	 */
	public final Enrollment getEnrollment() {
		return enrollment;
	}

	/**
	 * @param enrollment
	 *            the enrollment to set
	 */
	public final void setEnrollment(Enrollment enrollment) {
		this.enrollment = enrollment;
	}

	/**
	 * @return the mspId
	 */
	public final String getMspId() {
		return mspId;
	}

	/**
	 * @param mspId
	 *            the mspId to set
	 */
	public final void setMspId(String mspId) {
		this.mspId = mspId;
	}

}
