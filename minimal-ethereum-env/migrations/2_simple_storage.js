const SimpleStorage = artifacts.require('SimpleStorage');

module.exports = async deployer => {
  await deployer.deploy(SimpleStorage, 4);
};
