const SimpleStorage = artifacts.require("SimpleStorage");

module.exports = async done => {
    const simpleStorage = await SimpleStorage.deployed();
    const address = simpleStorage.address;
    const code = await web3.eth.getCode(address);
    console.log(address);
    console.log(code);
    done();
};
