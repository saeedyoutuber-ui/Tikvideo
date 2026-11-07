const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = "demo-no-project";
const DATABASE_ID = "ai-studio-android-tikvideo-97c86ae1-0ddc-469d-8cf2-3666c1622f40";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

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

test("Unauthenticated user: cannot read videos", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("videos").get());
});

test("Unauthenticated user: cannot write profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      name: "Alice",
      handle: "@alice",
      createdAt: new Date(),
    })
  );
});

test("Authenticated user Alice: can create her own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      name: "Alice",
      handle: "@alice",
      createdAt: new Date(),
    })
  );
});

test("Authenticated user Bob: cannot update Alice's profile", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      name: "Alice",
      handle: "@alice",
      createdAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      name: "Hacked Alice",
    })
  );
});

test("Authenticated user Alice: can publish video", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("videos").doc("vid_101").set({
      userId: ALICE_UID,
      creatorName: "Alice",
      creatorHandle: "@alice",
      caption: "Check out this TikVideo reel!",
      soundTitle: "Original Sound",
      category: "FOR_YOU",
      likesCount: 0,
      createdAt: new Date(),
    })
  );
});

test("Authenticated user Bob: cannot delete Alice's video", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("videos").doc("vid_101").set({
      userId: ALICE_UID,
      creatorName: "Alice",
      creatorHandle: "@alice",
      caption: "My video",
      soundTitle: "Sound",
      category: "FOR_YOU",
      createdAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("videos").doc("vid_101").delete());
});
