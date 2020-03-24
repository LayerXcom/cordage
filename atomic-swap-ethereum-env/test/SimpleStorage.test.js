const simpleStorage = artifacts.require('Settlement.sol');
const BN = require('bn.js');
const chai = require('chai');
chai.use(require('chai-bn')(BN));
const expect = chai.expect;

contract('SimpleStorage', accounts => {
  const deployer = accounts[0];

  beforeEach(async () => {
    this.simpleStorage = await simpleStorage.new(1, { from: deployer });
  });

  describe('set', () => {
    it('emits Set', async () => {
      const value = new BN('100000000', 10);
      const tx = await this.simpleStorage.set(value);
      const eventName = tx.logs[0].event;
      const args = tx.logs[0].args;

      expect(eventName).to.equal('Set');
      expect(args[0]).to.bignumber.equal(value);
    });
  });
});
