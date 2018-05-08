const shim = require('fabric-shim');

const PradeepChaincode = class {

async Init(stub) {

    return shim.success(Buffer.from('Initialization was Successfull!'));

 }

async Invoke(stub) {

    return shim.success(Buffer.from('Initialization was Successfull!'));
 }

};

shim.start(new PradeepChaincode());
