import random

from BasicTest import *

class RandomCorruptTest(BasicTest):
    def handle_packet(self):
        for p in self.forwarder.in_queue:
            if random.choice([True, False]):
                p.update_packet(data="Bad Data 1234", update_checksum=False)

            self.forwarder.out_queue.append(p)

        # empty out the in_queue
        self.forwarder.in_queue = []
