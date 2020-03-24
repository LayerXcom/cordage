const Settlement = artifacts.require('Settlement');

module.exports = async done => {
  const settlement = await Settlement.deployed();
  const address = settlement.address;
  const code = await web3.eth.getCode(address);
  console.log(address);
  console.log(code);
  done();
};
