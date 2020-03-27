import random

from BasicTest import *

class DropAndDuplicatePacketTest(BasicTest):
    def handle_packet(self):
        for p in self.forwarder.in_queue:

            # Every packet is either dropped or duplicated
            if random.choice([True, False]):
            	# for i in range(0, random.randint(1, 2)):
            	self.forwarder.out_queue.append(p)
            	self.forwarder.out_queue.append(p)
        	# else:
        	# 	pass

        # empty out the in_queue
        self.forwarder.in_queue = []
