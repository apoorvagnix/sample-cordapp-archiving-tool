# cordapp-example 

This cordapp-example application allows a party (lender)  to lend an Amount to another party (borrower).


## States

* `CustomState`: This is a [LinearState](https://docs.r3.com/en/platform/corda/4.9/community/api-states.html#linearstate) that represents an IOU that can be issued by one party (lender) to another party (borrower).

## Contracts

* `CustomContract`: This is used to govern the evolution of an CustomState.

## Flows

* `CustomIssueFlow`: This flow is used to create an `CustomState`. It takes 2 arguments as the parameters: the `amount` (Int) and the `participantB` (Party).
* `CustomConsumeFlow`: This flow is used to consume the `CustomState`. It takes 1 argument as the parameters: the `stateLinearId` (UUID).

## Pre-requisites:
[Set up for CorDapp development](https://docs.r3.com/en/platform/corda/4.9/community/getting-set-up.html)

## Running the nodes:
1. Open a terminal and go to the project root directory and type: (to deploy the nodes using bootstrapper)
```
./gradlew clean build deployNodes
```
2. Then type: (to run the nodes)
```
./build/nodes/runnodes
```
This should open up 3 new tabs in the terminal window with Corda interactive shells. 

One for the Notary, one for Party A, and one for Party B.
(If any of the nodes is missing a Corda interactive shell, from the root folder, navigate to ```./build/node/{missing party node}``` and run ```java -jar corda.jar``` to boot up the Corda interactive shell manually.)

3. Next, navigate to the Party A Corda interactive shell to start the `ExampleFlow` to issue an IOU (from Party A (lender) to Party B (borrower)). Type the following command:
```

flow start CustomIssueFlow$IssueFlow amount: 100, participantB: "O=PartyB,L=New York,C=US"

flow start CustomConsumeFlow$ConsumeFlow stateLinearId: "<linear-id-obtained-from-CustomIssueFlow$IssueFlow>"

```


4. To check that you've successfully issued an IOU from Party A to Party B, navigate to the Party B Corda interactive shell to check all of the existing `CustomState` in Party B's vault. Type:
```

run vaultQuery contractStateType: "net.corda.samples.example.states.CustomState"

```
This command will output all the States in Party B's vault which has a contract state type of `CustomState`.

You've now successfully issued and consumed a `CustomState` of amount 100, from Party A to Party B!


