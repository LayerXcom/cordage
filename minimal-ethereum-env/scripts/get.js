const SimpleStorage = artifacts.require("SimpleStorage");

module.exports = async done => {
    const simpleStorage = await SimpleStorage.deployed();
    const data = await simpleStorage.get();
    console.log("Data:", data.toString(10));
    done();
};
