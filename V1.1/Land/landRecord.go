package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"strconv"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	"github.com/hyperledger/fabric/protos/peer"
)

// Commands to run
// peer chaincode install -p chaincodedev/chaincode/land -n mycc -v 0
// peer chaincode instantiate -n mycc -v 0 -c '{"Args":[]}' -C myc
// peer chaincode invoke -n mycc -c '{"Args":["initLedger"]}' -C myc
// peer chaincode query -n mycc -c '{"Args":["queryLedger","PID1"]}' -C myc
// peer chaincode query -n mycc -c '{"Args":["queryAllLands"]}' -C myc
// peer chaincode invoke -n mycc -c '{"Args":["queryAllLands"]}' -C myc
// peer chaincode invoke -n mycc -c '{"Args":["createLand","PID6","Bangalore", "AGRI", "CREATED", "DEEPU"]}' -C myc
// peer chaincode invoke -n mycc -c '{"Args":["changeLandOwner","PID6","CHANDAN"]}' -C myc
// peer chaincode invoke -n mycc -c '{"Args":["changeLandStatus","PID6","FOR_SALE"]}' -C myc
// peer chaincode invoke -n mycc -c '{"Args":["deleteLand","PID6"]}' -C myc

//LandRecordSmartContract : Smart contract struct
type LandRecordSmartContract struct {
}

//LandRecordSmartContract : Name of the struct
type Land struct {
	Location string `json:"location"`
	Type     string `json:"type"`
	Status   string `json:"status"`
	Owner    string `json:"owner"`
}

// Init : Method to initialize or update the chaincode
func (s *LandRecordSmartContract) Init(APIstub shim.ChaincodeStubInterface) peer.Response {
	return shim.Success(nil)
}

// Invoke : Method to transaction between the ledger
func (s *LandRecordSmartContract) Invoke(APIstub shim.ChaincodeStubInterface) peer.Response {

	funcName, args := APIstub.GetFunctionAndParameters()

	switch funcName {
	case "initLedger":
		fmt.Println("Calling initLedger()")
		return s.initLedger(APIstub)
	case "queryLedger":
		fmt.Println("Calling queryLedger()")
		return s.queryLedger(APIstub, args)
	case "queryAllLands":
		fmt.Println("Calling queryAllLands()")
		return s.queryAllLands(APIstub)
	case "createLand":
		fmt.Println("Calling createLand()")
		return s.createLand(APIstub, args)
	case "changeLandOwner":
		fmt.Println("Calling changeLandOwner()")
		return s.changeLandOwner(APIstub, args)
	case "changeLandStatus":
		fmt.Println("Calling changeLandStatus()")
		return s.changeLandStatus(APIstub, args)
	case "deleteLand":
		fmt.Println("Calling changeLandStatus()")
		return s.deleteLand(APIstub, args)
	default:
		return shim.Error("Unknown Function name")
	}

}

// initLedger : populating the ledger
func (s *LandRecordSmartContract) initLedger(APIstub shim.ChaincodeStubInterface) peer.Response {

	lands := []Land{
		Land{"Bangalore", "AGRI", "CREATED", "RAJ"},
		Land{"Tumkur", "PRIVATE", "CREATED", "HITESH"},
		Land{"Tumkur", "AGRI", "CREATED", "PRADEEP"},
		Land{"Raichur", "PRIVATE", "CREATED", "RAJ"},
		Land{"Bidar", "PRIVATE", "CREATED", "RAM"},
		Land{"Bidar", "AGRI", "CREATED", "SHYAM"},
	}

	for i := 0; i < len(lands); i++ {

		fmt.Printf("Inserting Land of index %v with land details %v\n", i, lands[i])

		landAsByte, err := json.Marshal(lands[i])

		if err != nil {
			fmt.Printf("Error while adding the land of index %v with land details %v to ledger.\n Error : %v\n ", i, lands[i], err)
		}

		APIstub.PutState("PID"+strconv.Itoa(i), landAsByte)
		fmt.Printf("Added land with index %v successfully.\n", i)

	}

	return shim.Success(nil)
}

// queryLedger : Query the Land with PID
func (s *LandRecordSmartContract) queryLedger(APIstub shim.ChaincodeStubInterface, args []string) peer.Response {

	if len(args) != 1 {
		return shim.Error("Incorrect Arguements")

	}

	landAsByte, _ := APIstub.GetState(args[0])
	fmt.Println(string(landAsByte))

	return shim.Success(landAsByte)
}

// queryAllLands : Fecthing all the lands for the range 1 : 999
func (s *LandRecordSmartContract) queryAllLands(APIstub shim.ChaincodeStubInterface) peer.Response {

	startKey := "PID1"
	endKey := "PID999"

	resultsIterator, err := APIstub.GetStateByRange(startKey, endKey)
	if err != nil {
		return shim.Error(err.Error())
	}
	defer resultsIterator.Close()

	var buffer bytes.Buffer
	buffer.WriteString("[")

	bArrayMemberAlreadyWritten := false
	for resultsIterator.HasNext() {
		queryResponse, err := resultsIterator.Next()
		if err != nil {
			return shim.Error(err.Error())
		}

		if bArrayMemberAlreadyWritten == true {
			buffer.WriteString(",")
		}
		buffer.WriteString("{\"Key\":")
		buffer.WriteString("\"")
		buffer.WriteString(queryResponse.Key)
		buffer.WriteString("\"")

		buffer.WriteString(", \"Record\":")
		// Record is a JSON object, so we write as-is
		buffer.WriteString(string(queryResponse.Value))
		buffer.WriteString("}")
		bArrayMemberAlreadyWritten = true
	}
	buffer.WriteString("]")

	fmt.Printf("- queryAllLands:\n%s\n", buffer.String())

	return shim.Success(buffer.Bytes())

}

//createLand : Create a new land record
func (s *LandRecordSmartContract) createLand(APIstub shim.ChaincodeStubInterface, args []string) peer.Response {

	if len(args) != 5 {
		return shim.Error("Incorrect number of arguments. Expecting 5")
	}

	var land = Land{Location: args[1], Type: args[2], Status: args[3], Owner: args[4]}

	landAsBytes, _ := json.Marshal(land)
	err := APIstub.PutState(args[0], landAsBytes)

	if err != nil {
		return shim.Error("Error while creating the Land" + err.Error())
	}

	return shim.Success(nil)
}

//changeLandOwner : Change the land owner for the given PID
func (s *LandRecordSmartContract) changeLandOwner(APIstub shim.ChaincodeStubInterface, args []string) peer.Response {

	if len(args) != 2 {
		return shim.Error("Incorrect number of arguments. Expecting 2")
	}

	landAsBytes, _ := APIstub.GetState(args[0])
	land := Land{}

	json.Unmarshal(landAsBytes, &land)
	land.Owner = args[1]

	landAsBytes, _ = json.Marshal(land)
	APIstub.PutState(args[0], landAsBytes)

	return shim.Success(landAsBytes)
}

//changeLandStatus : Change the land status for the given PID
func (s *LandRecordSmartContract) changeLandStatus(APIstub shim.ChaincodeStubInterface, args []string) peer.Response {

	if len(args) != 2 {
		return shim.Error("Incorrect number of arguments. Expecting 2")
	}

	landAsBytes, _ := APIstub.GetState(args[0])
	land := Land{}

	json.Unmarshal(landAsBytes, &land)
	land.Status = args[1]

	landAsBytes, _ = json.Marshal(land)
	APIstub.PutState(args[0], landAsBytes)

	return shim.Success(landAsBytes)
}

//deleteLand : Delete the land for the given PID
func (s *LandRecordSmartContract) deleteLand(APIstub shim.ChaincodeStubInterface, args []string) peer.Response {
	if len(args) != 1 {
		return shim.Error("Incorrect number of arguments. Expecting 1")
	}

	err := APIstub.DelState(args[0])
	if err != nil {
		return shim.Error("Failed to delete land")
	}

	return shim.Success(nil)
}

func main() {

	err := shim.Start(new(LandRecordSmartContract))

	if err != nil {
		fmt.Printf("Error Creating the new smart contract LandRecordSmartContract : %s", err)
	}

}
