import shim from "fabric-shim";
import util from "util";

let LandChaincode = class {

    async Init(stub) {
        console.info('=========== Instantiated land record chaincode ===========');
        return shim.success();
    }


    async Invoke(stub) {
        let ret = stub.getFunctionAndParameters();
        console.info(ret);

        let method = this[ret.fcn];
        if (!method) {
            console.error('no function of name:' + ret.fcn + ' found');
            throw new Error('Received unknown function ' + ret.fcn + ' invocation');
        }
        try {
            let payload = await method(stub, ret.params);
            return shim.success(payload);
        } catch (err) {
            console.log(err);
            return shim.error(err);
        }
    }

    async queryLand(stub, args) {
        if (args.length != 1) {
            throw new Error('Incorrect number of arguments. Expecting Land PID number ex: PID01');
        }

        let landNumber = args[0];
        let landAsBytes = await stub.getState(landNumber);
        if (!landAsBytes || landAsBytes.toString().length <= 0) {
            throw new Error(landNumber + ' does not exist: ');
        }

        console.log(landAsBytes.toString());
        return landAsBytes;
    }


    async createLand(stub, args) {
        console.info('Calling createLand()');
        if (args.length != 5) {
            throw new Error('Incorrect number of arguments. Expecting 5');
        }

        var land = {
            docType: 'land',
            location: args[1],
            type: args[2],
            status: args[3],
            owner: args[4]
        };

        await stub.putState(args[0], Buffer.from(JSON.stringify(land)));
    };


    async changeLandOwner(stub, args) {
        console.info('calling changeLandOwner()');
        if (args.length != 2) {
            throw new Error('Incorrect number of arguments. Expecting 2');
        }

        let landAsBytes = await stub.getState(args[0]);
        let land = JSON.parse(landAsBytes);
        land.owner = args[1];

        await stub.putState(args[0], Buffer.from(JSON.stringify(land)));
    }


    async changeLandStatus(stub, args) {
        console.info('calling changeLandStatus()');
        if (args.length != 2) {
            throw new Error('Incorrect number of arguments. Expecting 2');
        }

        let landAsBytes = await stub.getState(args[0]);
        let land = JSON.parse(landAsBytes);
        land.status = args[1];

        await stub.putState(args[0], Buffer.from(JSON.stringify(land)));
    }


    async initLedger(stub, args) {

        console.info('============= Initializing of Land Ledger Started ===========');
        let lands = [];

        lands.push({
            location: 'Bangalore',
            type: 'AGRI',
            status: 'CREATED',
            owner: 'RAJ'
        });

        lands.push({
            location: 'Tumkur',
            type: 'PRIVATE',
            status: 'CREATED',
            owner: 'HITESH'
        });


        lands.push({
            location: 'Tumkur',
            type: 'AGRI',
            status: 'CREATED',
            owner: 'PRADEEP'
        });

        lands.push({
            location: 'Raichur',
            type: 'PRIVATE',
            status: 'CREATED',
            owner: 'RAJ'
        });

        lands.push({
            location: 'Bidar',
            type: 'PRIVATE',
            status: 'CREATED',
            owner: 'RAM'
        });

        lands.push({
            location: 'Bidar',
            type: 'AGRI',
            status: 'CREATED',
            owner: 'SHYAM'
        });

        for (let index = 0; index < lands.length; index++) {
            lands[i].docType = 'land';
            await stub.putState('PID' + i, Buffer.from(JSON.stringify(lands[i])));
            console.info('Added', lands[i]);
        }

        console.info('============= Initializing of Land Ledger Ended===========');
    };




};

shim.start(new LandChaincode())
