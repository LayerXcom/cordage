const SimpleStorage = artifacts.require("SimpleStorage");
const BN = require('bn.js');

module.exports = async done => {
    // parse new data from arguments
    let amount;
    if (!isNaN(parseInt(process.argv[4], 10))) {
        amount = new BN(process.argv[4], 10)
    }

    // get previous data
    const simpleStorage = await SimpleStorage.deployed();
    const before = await simpleStorage.get();
    console.log("Previous data:", before.toString(10));

    // update with new data
    if (!amount) {
        amount = before.add(new BN("1", 10))
    }
    await simpleStorage.set(amount);
    const newData = await simpleStorage.get();
    console.log("New data:", newData.toString(10));
    done();
};
