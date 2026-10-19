const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = "demo-no-project";

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules: fs.readFileSync("firestore.rules", "utf8"),
      host: "127.0.0.1",
      port: 8085,
    },
  });
});

beforeEach(async () => {
  await testEnv.clearFirestore();
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

test("unauthenticated users cannot read or write anything", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc("alice").get());
  await assertFails(
    unauthDb.collection("users").doc("alice").set({
      id: "alice",
      displayName: "Alice",
      email: "alice@example.com",
    })
  );
  await assertFails(unauthDb.collection("rooms").doc("room1").get());
});

test("authenticated user can create own profile and read profiles", async () => {
  const aliceDb = testEnv.authenticatedContext("alice").firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc("alice").set({
      id: "alice",
      displayName: "Alice Smith",
      email: "alice@example.com",
      avatarColorHex: "#10B981",
      createdAt: Date.now(),
    })
  );

  const bobDb = testEnv.authenticatedContext("bob").firestore();
  await assertSucceeds(bobDb.collection("users").doc("alice").get());
  await assertFails(
    bobDb.collection("users").doc("alice").set({
      id: "alice",
      displayName: "Mallory",
      email: "mal@example.com",
    })
  );
});

test("authenticated user can create room and add entries", async () => {
  const aliceDb = testEnv.authenticatedContext("alice").firestore();
  await assertSucceeds(
    aliceDb.collection("rooms").doc("room1").set({
      id: "room1",
      name: "Apartment 4B",
      inviteCode: "FLAT4B",
      createdBy: "alice",
      createdAt: Date.now(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("rooms").doc("room1").collection("members").doc("alice").set({
      userId: "alice",
      displayName: "Alice",
      avatarColorHex: "#10B981",
      joinedAt: Date.now(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("rooms").doc("room1").collection("entries").doc("entry1").set({
      id: "entry1",
      amount: 450,
      category: "Groceries",
      itemName: "Milk, Bread & Veggies",
      paidByMemberId: "alice",
      paidByMemberName: "Alice",
      date: "2026-10-18",
      createdAt: Date.now(),
    })
  );

  // Invalid entry: negative amount must fail
  await assertFails(
    aliceDb.collection("rooms").doc("room1").collection("entries").doc("entry2").set({
      id: "entry2",
      amount: -50,
      category: "Groceries",
      itemName: "Bad Entry",
      paidByMemberId: "alice",
      date: "2026-10-18",
    })
  );
});
