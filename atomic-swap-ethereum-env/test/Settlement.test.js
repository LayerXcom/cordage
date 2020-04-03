const settlement = artifacts.require('Settlement.sol');
const truffleAssert = require('truffle-assertions');
const BN = require('bn.js');
const chai = require('chai');
chai.use(require('chai-bn')(BN));
const expect = chai.expect;

contract('Settlement', accounts => {
  const deployerAddress = accounts[0];
  const proposerAddress = accounts[1];
  const acceptorAddress = accounts[2];
  const swapId = '1';
  const weiAmount = new BN('100000000', 10);
  const securityAmount = new BN('1000', 10);

  beforeEach(async () => {
    this.settlement = await settlement.new({ from: deployerAddress });
  });

  describe('lock', () => {
    it('emits Locked event', async () => {
      const tx = await this.settlement.lock(
        swapId,
        proposerAddress,
        acceptorAddress,
        weiAmount,
        securityAmount,
        { value: weiAmount },
        { from: proposerAddress },
      );
      const eventName = tx.logs[0].event;
      expect(eventName).to.equal('Locked');
      const args = tx.logs[0].args;
      expect(args.swapId).to.equal(swapId);
    });

    it('cannot execute when msg.sender is not equal to transferFromAddress', async () => {
      await truffleAssert.reverts(
        this.settlement.lock(
          swapId,
          proposerAddress,
          acceptorAddress,
          weiAmount,
          securityAmount,
          {value: weiAmount},
          {from: acceptorAddress},
        ),
        'msg.sender is not _transferFromAddress',
      );
    });

    it('cannot execute when msg.value is not equivalent to weiAmount', async () => {
      await truffleAssert.reverts(
        this.settlement.lock(
          swapId,
          proposerAddress,
          acceptorAddress,
          weiAmount,
          securityAmount,
          { value: new BN('50000', 10) },
          { from: proposerAddress },
        ),
        'msg.value is not equivalent to _weiAmount',
      );
    });
  });

  describe('unlock', () => {
    it('emits Unlocked event', async () => {
      const tx = await this.settlement.unlock(
        swapId,
        proposerAddress,
        acceptorAddress,
        weiAmount,
        { from: deployerAddress }
      );
      const eventName = tx.logs[0].event;
      expect(eventName).to.equal('Unlocked');
      const args = tx.logs[0].args;
      expect(args.swapId).to.equal(swapId);
    });

    it('cannot execute from not owner', async () => {
      await truffleAssert.reverts(
        this.settlement.unlock(
          swapId,
          proposerAddress,
          acceptorAddress,
          weiAmount,
          { from: acceptorAddress },
        ),
        'msg.sender is not contract owner',
      );
    });

    it('cannot unlock non-existent swapId', async () => {
      await truffleAssert.reverts(
        this.settlement.unlock(
          'BAD_SWAPID',
          proposerAddress,
          acceptorAddress,
          weiAmount,
          { from: deployerAddress },
        ),
        'The swapId does not exist',
      );
    });
  });

  describe('abort', () => {
    it('emits Aborted event', async () => {
      const tx = await this.settlement.abort(
        swapId,
        proposerAddress,
        acceptorAddress,
        weiAmount,
        { from: deployerAddress },
      );
      const eventName = tx.logs[0].event;
      expect(eventName).to.equal('Aborted');
      const args = tx.logs[0].args;
      expect(args.swapId).to.equal(swapId);
    });

    it('cannot execute from not owner', async () => {
      await truffleAssert.reverts(
        this.settlement.abort(
          swapId,
          proposerAddress,
          acceptorAddress,
          weiAmount,
          { from: proposerAddress },
        ),
        'msg.sender is not contract owner',
      );
    });

    it('cannot unlock non-existent swapId', async () => {
      await truffleAssert.reverts(
        this.settlement.unlock(
          'BAD_SWAPID',
          proposerAddress,
          acceptorAddress,
          weiAmount,
          { from: deployerAddress },
        ),
        'The swapId does not exist',
      );
    });
  });
});
