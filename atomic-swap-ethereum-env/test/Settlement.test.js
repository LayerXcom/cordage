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
  const notaryAddress = accounts[3];
  let swapIdNumber = 1;
  let swapId = '1';
  const weiAmount = new BN('100000000', 10);
  const securityAmount = new BN('1000', 10);

  beforeEach(async () => {
    this.settlement = await settlement.new(notaryAddress, { from: deployerAddress });
  });

  afterEach(async () => {
    swapIdNumber++;
    swapId = swapIdNumber.toString(10);
  });

  describe('lock', () => {
    it('emits Locked event', async () => {
      const tx = await this.settlement.lock(
        swapId,
        proposerAddress,
        acceptorAddress,
        weiAmount,
        securityAmount,
        { from: proposerAddress, value: weiAmount },
      );
      const eventName = tx.logs[0].event;
      expect(eventName).to.equal('Locked');
      const args = tx.logs[0].args;
      expect(args.swapId).to.equal(swapId);

      const actual = await this.settlement.swapIdToDetailMap.call(swapId);
      const decodedSwapDetail = web3.eth.abi.decodeParameter({
        SwapDetail: {
          transferFromAddress: 'address',
          transferToAddress: 'address',
          weiAmount: 'uint256',
          securityAmount: 'uint256',
          status: 'uint8',
        },
      }, args.encodedSwapDetail);

      expect(decodedSwapDetail.transferFromAddress).to.equal(actual.transferFromAddress);
      expect(decodedSwapDetail.transferToAddress).to.equal(actual.transferToAddress);
      expect(decodedSwapDetail.weiAmount).to.be.bignumber.equal(actual.weiAmount);
      expect(decodedSwapDetail.securityAmount).to.be.bignumber.equal(actual.securityAmount);
      expect(decodedSwapDetail.status).to.be.bignumber.equal(actual.status);
    });

    it('cannot execute when msg.sender is not equal to transferFromAddress', async () => {
      await truffleAssert.reverts(
        this.settlement.lock(
          swapId,
          proposerAddress,
          acceptorAddress,
          weiAmount,
          securityAmount,
          { from: acceptorAddress, value: weiAmount },
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
          { from: proposerAddress, value: new BN('50000', 10) },
        ),
        'msg.value is not equivalent to _weiAmount',
      );
    });
  });

  describe('unlock', () => {
    beforeEach(async () => {
      await this.settlement.lock(
        swapId,
        proposerAddress,
        acceptorAddress,
        weiAmount,
        securityAmount,
        { from: proposerAddress, value: weiAmount },
      );
    });

    it('emits Unlocked event', async () => {
      const tx = await this.settlement.unlock(
        swapId,
        { from: notaryAddress },
      );
      const eventName = tx.logs[0].event;
      expect(eventName).to.equal('Unlocked');
      const args = tx.logs[0].args;
      expect(args.swapId).to.equal(swapId);

      const actual = await this.settlement.swapIdToDetailMap.call(swapId);
      const decodedSwapDetail = web3.eth.abi.decodeParameter({
        SwapDetail: {
          transferFromAddress: 'address',
          transferToAddress: 'address',
          weiAmount: 'uint256',
          securityAmount: 'uint256',
          status: 'uint8',
        },
      }, args.encodedSwapDetail);

      expect(decodedSwapDetail.transferFromAddress).to.equal(actual.transferFromAddress);
      expect(decodedSwapDetail.transferToAddress).to.equal(actual.transferToAddress);
      expect(decodedSwapDetail.weiAmount).to.be.bignumber.equal(actual.weiAmount);
      expect(decodedSwapDetail.securityAmount).to.be.bignumber.equal(actual.securityAmount);
      expect(decodedSwapDetail.status).to.be.bignumber.equal(actual.status);
    });

    it('cannot execute unless it is from corda notary', async () => {
      await truffleAssert.reverts(
        this.settlement.unlock(
          swapId,
          { from: acceptorAddress },
        ),
        'caller is not the corda notary',
      );
    });

    it('cannot unlock nonexistent swapId', async () => {
      await truffleAssert.reverts(
        this.settlement.unlock(
          'BAD_SWAPID',
          { from: notaryAddress },
        ),
        'swapDetail does not exist',
      );
    });

    it('cannot unlock if the swapId status is Unlocked', async () => {
      await this.settlement.unlock(
        swapId,
        { from: notaryAddress },
      );

      await truffleAssert.reverts(
        this.settlement.unlock(
          swapId,
          { from: notaryAddress },
        ),
        'swapDetail.status is not Locked',
      );
    });
  });
});
