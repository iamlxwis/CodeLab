import unittest
from require_release_acceptance import GROUPS, problems

class ReleaseAcceptanceTest(unittest.TestCase):
    def ledger(self):
        return '\n'.join(f'| {group}{i:02} | Criterion | Accepted | Required check | Verified: run evidence |'
                         for group, count in GROUPS.items() for i in range(1, count + 1))

    def test_empty_partial_and_source_only_ledgers_fail_closed(self):
        self.assertTrue(problems(''))
        self.assertTrue(problems(self.ledger().replace('W09', 'W99')))
        self.assertTrue(problems(self.ledger().replace('Accepted', 'Implemented', 1)))
        self.assertTrue(problems(self.ledger().replace('Verified: run evidence', 'Source only', 1)))

    def test_complete_evidenced_ledger_and_duplicate_detection(self):
        text = self.ledger()
        self.assertFalse(problems(text))
        self.assertTrue(problems(text + '\n' + text.splitlines()[0]))
