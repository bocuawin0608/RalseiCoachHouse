import fs from 'node:fs';
import path from 'node:path';

export default class StatusReporter {
  constructor() {
    this.results = [];
  }

  onTestEnd(test, result) {
    const status = result.status.toUpperCase();
    const record = {
      id: test.title.match(/\b(?:BK-)?TC-[A-Z]?\d+[a-z]?\b/i)?.[0] || test.title,
      title: test.title,
      status,
      durationMs: result.duration,
      error: result.error?.message || null,
    };
    this.results.push(record);
    console.log(`[BLACK-BOX TEST] ${record.id}: ${status} (${record.durationMs} ms)`);
  }

  onEnd(result) {
    const reportDir = path.resolve('reports');
    fs.mkdirSync(reportDir, { recursive: true });
    const summary = {
      generatedAt: new Date().toISOString(),
      overallStatus: result.status.toUpperCase(),
      totals: this.results.reduce((totals, test) => {
        totals[test.status] = (totals[test.status] || 0) + 1;
        return totals;
      }, {}),
      tests: this.results,
    };
    fs.writeFileSync(path.join(reportDir, 'latest-status.json'), JSON.stringify(summary, null, 2));
  }
}
