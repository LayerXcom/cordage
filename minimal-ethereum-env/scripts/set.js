const SimpleStorage = artifacts.require("SimpleStorage");
const BN = require('bn.js');

module.exports = async done => {
    const simpleStorage = await SimpleStorage.deployed();
    const data = await simpleStorage.get();
    console.log("Previous data:", data.toString(10));

    await simpleStorage.set(data.add(new BN("1", 10)));
    const newData = await simpleStorage.get();
    console.log("New data:", newData.toString(10));
    done();
};
