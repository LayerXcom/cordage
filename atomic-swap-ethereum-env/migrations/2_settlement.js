const Settlement = artifacts.require('Settlement');

module.exports = async (deployer, network, accounts) => {
  const notaryAddress = accounts[3];
  await deployer.deploy(Settlement, notaryAddress);
};
