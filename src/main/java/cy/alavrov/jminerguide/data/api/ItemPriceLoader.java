/*
 * Copyright (c) 2015, Andrey Lavrov <lavroff@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package cy.alavrov.jminerguide.data.api;

import cy.alavrov.jminerguide.data.character.APIException;
import cy.alavrov.jminerguide.data.price.ItemPriceContainer;
import cy.alavrov.jminerguide.data.universe.MarketZone;

/**
 * A task to load prices from the EveCentral
 * @author Andrey Lavrov <lavroff@gmail.com>
 */
public class ItemPriceLoader implements Runnable{
    
    private final ItemPriceContainer target;
    private final IItemPriceLoadingResultReceiver receiver;
    private final MarketZone zone;
    
    public ItemPriceLoader(ItemPriceContainer target, IItemPriceLoadingResultReceiver receiver, MarketZone zone) {
        this.target = target;
        this.receiver = receiver;
        this.zone = zone;
    }
    
    @Override
    public void run() {
        try {
            target.loadFromEVECEntral(zone);
        } catch (APIException e) {
            final String message = e.getMessage();
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    receiver.loadingDone(false, message);
                }
            });
            return;
        }
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                receiver.loadingDone(true, "OK");
            }
        });
    }
    
}
