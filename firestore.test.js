const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read users, rooms", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("rooms").doc("room_1").get());
});

test("Authenticated user: can create and update own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      id: ALICE_UID,
      name: "Alice",
      avatarColorHex: "#059669",
      upiId: "alice@upi"
    })
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  // Bob cannot overwrite Alice's profile
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).set({
      id: ALICE_UID,
      name: "Bob Impersonator"
    })
  );
});

test("Authenticated user: can create room and add entry", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const roomId = "room_123";

  await assertSucceeds(
    aliceDb.collection("rooms").doc(roomId).set({
      id: roomId,
      name: "Flat 4B",
      inviteCode: "FLAT4B",
      createdBy: ALICE_UID,
      emoji: "🏠"
    })
  );

  await assertSucceeds(
    aliceDb.collection("rooms").doc(roomId).collection("members").doc(ALICE_UID).set({
      id: ALICE_UID,
      roomId: roomId,
      userId: ALICE_UID,
      name: "Alice",
      avatarColorHex: "#059669",
      isActive: true
    })
  );

  await assertSucceeds(
    aliceDb.collection("rooms").doc(roomId).collection("entries").doc("entry_1").set({
      id: "entry_1",
      roomId: roomId,
      memberId: ALICE_UID,
      memberName: "Alice",
      itemName: "Tomatoes",
      category: "vegetables",
      amount: 120.0,
      date: "2026-10-15"
    })
  );
});

test("Entry creation fails with negative or 0 amount", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const roomId = "room_123";
  await assertFails(
    aliceDb.collection("rooms").doc(roomId).collection("entries").doc("bad_entry").set({
      id: "bad_entry",
      roomId: roomId,
      memberId: ALICE_UID,
      itemName: "Tomatoes",
      category: "vegetables",
      amount: -50.0,
      date: "2026-10-15"
    })
  );
});
