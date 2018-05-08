// package main

// import (
// 	"fmt"

// 	"github.com/hyperledger/fabric/core/chaincode/shim"
// 	"github.com/hyperledger/fabric/protos/peer"
// )

// //SmartContract : name of the smart contract
// type SmartContract struct{}

// // Init : Method to initialize or update the chaincode
// func (s *SmartContract) Init(APIstub shim.ChaincodeStubInterface) peer.Response {
// 	return shim.Success(nil)
// }

// // Invoke : Method to transaction between the ledger
// func (s *SmartContract) Invoke(APIstub shim.ChaincodeStubInterface) peer.Response {
// 	return shim.Error("Invalid Smart Contract function name.")
// }

// func main() {

// 	err := shim.Start(new(SmartContract))

// 	if err != nil {
// 		fmt.Printf("Error Creating the new smart contract SmartContract : %s", err)
// 	}

// }
