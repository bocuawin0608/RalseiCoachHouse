# Scenario B failed API tests

This collection contains only the Scenario B rows currently marked **Failed**:
`B02`, `B03`, `B06`, `B07`, `B13`, `B14`, `B18`, `B19`, `B21`, `B22`, `B26`, `B34`,
`B37`, `B40`, and `B41`.

## Run in Postman

1. Start Spring Boot on `https://localhost:9090`.
2. Import `trip-search-negative-tests.postman_collection.json`.
3. Edit collection variables when needed:
   - `baseUrl`: backend API root.
   - `searchDate`: a seeded future date; blank means 30 days from today.
   - `route`: a seeded normal route.
   - `hyphenatedRoute`: a seeded route whose name contains two or more hyphens.
4. If using the local self-signed certificate, disable **SSL certificate
   verification** in Postman settings.
5. Choose **Run collection**. Use 1 iteration for diagnosis, then 50-500
   iterations with a modest delay for pressure testing.

The collection is read-only. Pressure testing is still load against a real
service, so use a test environment—not production.

## Run with Newman

```bash
npx newman run test/postman/trip-search-negative-tests.postman_collection.json \
  --insecure --iteration-count 100 --delay-request 50
```

These tests assert the intended safe contract. Red results are expected for
unfixed defects. Cases B13 and B18 choose the spreadsheet-permitted “reject”
behavior (HTTP 400) instead of silently truncating caller input.
