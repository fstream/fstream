/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package commands

import org.crsh.cli.Usage
import org.crsh.cli.Command
import org.crsh.command.InvocationContext

import io.fstream.simulate.core.Simulator
import io.fstream.simulate.message.Command as Message

/**
 * Commands related to the order book.
 */
class book {

  @Usage("Display order book to the console")
  @Command
  def main(InvocationContext context) {
    def simulator = getSimulator(context)
    simulator.exchange.tell(Message.PRINT_ORDER_BOOK, null)
    
    "The book has been printed to the console."
  }

  private Simulator getSimulator(InvocationContext context) {
    def beanFactory = context.attributes['spring.beanfactory']
    beanFactory.getBean(Simulator.class)
  }
  
}