const Settlement = artifacts.require('Settlement');

module.exports = async deployer => {
  await deployer.deploy(Settlement);
};
