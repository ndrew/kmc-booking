/////////////////////////////////////////////////////////////////////////////
// 
// Copyright (c) 2013 Andrew Sernyak
/////////////////////////////////////////////////////////////////////////////
//            DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
//                    Version 2, December 2004
//
// Copyright (C) 2004 Sam Hocevar <sam@hocevar.net>
//
// Everyone is permitted to copy and distribute verbatim or modified
// copies of this license document, and changing it is allowed as long
// as the name is changed.
//
//            DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
//   TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
//
//  0. You just DO WHAT THE FUCK YOU WANT TO.
/////////////////////////////////////////////////////////////////////////////
// This program is free software. It comes without any warranty, to
// the extent permitted by applicable law. You can redistribute it
// and/or modify it under the terms of the Do What The Fuck You Want
// To Public License, Version 2, as published by Sam Hocevar. See
// http://sam.zoy.org/wtfpl/COPYING for more details.
/////////////////////////////////////////////////////////////////////////////
  

/**
 * Main user interface controller/namespace
 */ 
function UI( data, prices ) {


  /**
   * Box - model for part of the theatre where usually people sit) 
   */
  function Box(rows, seats) {
    this.rows = rows;
    this.seats = seats;
  }


  /**
   * Separated ui model for box representation
   */
  function BoxUI( el, box ) {
    this.data = box
    this.el = $(el)
  }


  /**
   * Ui for multiple sub-boxes
   */
  function MultiBoxUI(parent_el, children, type ) {
    this.parent = parent_el
    this.children = children
    this.type = type  
  }

  /*
   * KMC layout -> DOM
   */
  function KMCSeatLayout() {

    var wing_rows = new Range(1, 11)
    var parter_rows = new Range(1, 13)
          
    var parter = $("#parter")
    var beletage = $("#back_house")

    var parts = [
      new MultiBoxUI( 
        parter, [
          new BoxUI( "#left_side_house",  new Box( wing_rows,   new Range(1, 3) ), parter),
          new BoxUI( "#left_house" ,    new Box( parter_rows,   new Range(4, 20) ), parter),
          new BoxUI( "#right_house"  ,    new Box( parter_rows,   new Range(21, 36) ), parter),
          new BoxUI( "#right_side_house" ,  new Box( wing_rows,   new Range(37, 39) ), parter),
        ], "1"),  // TODO: get rid of "1"
      new MultiBoxUI(
        beletage, [
          new BoxUI( "#beletage_last_row" ,   new Box( new Range(21, 21), new Range(1, 25) ), beletage),
          new BoxUI( "#beletage" ,      new Box( new Range(14, 20), new Range(1, 31) ), beletage)
        ], "2")   // TODO: get rid of "2"
    ]

    this.init_ui = function() {
      for (var i = parts.length - 1; i >= 0; i--) {
        init_multi( parts[i] )
      };
    }

    // TODO: refactor
    function init_multi(ui) {

      
      /**
       * Factory for creating dom elements 
       */
      function ElemFactory() {
        
        this.createSeatEl = function (seat, row, onclick) {
          var el = $("<div class='seat'/>").attr('x', seat).attr('y', row)
          el.attr('price', prices.getPrice(row,seat))
          el.append($("<p/>"))
          el.click(onclick)
          $("*", el).text(seat)
          return el
        }

        this.creteSeatNumEl = function (seat_num) {
          var el = $("<div/>").attr("class", "col_num")
          el.prepend($("<div/>").text(seat_num)) 
          return el
        }

        this.createRowNumEl = function(row_num) {
          var el = $("<div/>").attr("class", "row_num")
          el.append($("<div/>").text(row_num)) 
          return el
        }
      }

    
      var factory = new ElemFactory()


      function init(ui) {
        ui.data.rows.iterate( function(row) {
          var seat_el
          ui.data.seats.iterate( function(seat) {
            ui.el.append( factory.createSeatEl(seat, row, seat_onclick) ) 
          })
        })

        init_seat_nums( ui )
        init_row_nums( ui )
      }

      function init_row_nums( ui ) {

        switch(ui.type) {
          case "1":
            // max
            var max = 0
            var box
            for (var i = ui.children.length - 1; i >= 0; i--) {
              if ( ui.children[i].data.rows.size() > max ) {
                max = ui.children[i].data.rows.size()
                box = ui.children[i]
              }
            };

            ui.parent.prepend($("<div class='rows' style='float: left; margin-left: -15px;'></div>"))
            ui.parent.prepend($("<div class='rows' style='float: right; margin-right: -40px;'></div>"))

            $(".rows", ui.parent).prepend( factory.creteSeatNumEl("\u00a0") ) 
            box.data.rows.iterate( function(row) {
              // if ( $(".row_num>div:contains("+row+")", ui.parent).length === 0) {
              $(".rows", ui.parent).append( factory.createRowNumEl(row) ) 
              // }
            })

            $(".rows", ui.parent).prepend( factory.creteSeatNumEl("\u00a0") ) 

            break;
          case "2":
            ui.parent.prepend($("<div class='rows' style='float: left; margin-left: -25px;'></div>"))
            ui.parent.prepend($("<div class='rows' style='float: right; margin-right: -25px;'></div>"))
      
            for (var i = ui.children.length - 1; i >= 0; i--) {
              if ( ui.children[i].data.rows.size() !== 0 ) {
                $(".rows", ui.parent).prepend( factory.creteSeatNumEl("\u00a0") ) 
              }

              ui.children[i].data.rows.iterate( function(row) {
                // if ( $(".row_num>div:contains("+row+")", ui.parent).length === 0) {
                $(".rows", ui.parent).append( factory.createRowNumEl(row) ) 
                // }
              })
              $(".rows", ui.parent).append( factory.creteSeatNumEl("\u00a0") )  
            };
            break;
        }
      }


      function init_seat_nums( ui ) {
        
        var seat_nums = $("<div></div>");
        ui.data.seats.iterate( function(seat) {
          seat_nums.append( factory.creteSeatNumEl(seat) )  

        })

        if ( ui.data.rows.size() === 0 ) {
          ui.el.append(seat_nums)
        } else {
          ui.el.prepend(seat_nums)
          ui.el.append(seat_nums.clone())
        }
      }

      for (var i = ui.children.length - 1; i >= 0; i--) {
        init( ui.children[i] )
      };

      // init rows
      init_row_nums(ui)
    }

  }


  var layout = new KMCSeatLayout(); 
  var row_num_els = {}
  var seats_editable = false;
  
  function seat_onclick() {
    // console.log($(this).attr('x'), $(this).attr('y'), $(this).attr('price'))
    
    if (!seats_editable) {
      return;
    }

    if ( $(this).attr('booked') || $(this).attr('pending') ) {
      // console.log('can"t select seat')
      alert("Зайнято!")
      return;
    }

    if (typeof $(this).attr('your') == "undefined") { 
      $(this).addClass("seat_your");
      $(this).attr('your', 1);
    } else {
      var b = $(this).attr('your');
      $(this).toggleClass("seat_your");
      $(this).attr('your', (b == 0) ? 1 : 0);
    }
    update_status()
  }

  function update_status( ) {
    var total = 0
    $(".seat[your=1]").each(function() {
      total += parseInt($(this).attr('price'))
    })
    var status_text = "Ви забронювали " + $(".seat[your=1]").length + " квитків на суму "+total + ' грн.'
    
    $("#booking_status").text(status_text)
  }

  function set_data( new_data ) {
    // TODO: use cycle
    //console.log(new_data)

    if (typeof new_data.booked != "undefined") {
      for (var i = new_data.booked.length - 1; i >= 0; i--) {
        var seat_el = $(".seat[x="+new_data.booked[i].x+"][y="+new_data.booked[i].y+"]");
        seat_el.attr('booked', true);
        seat_el.addClass("seat_booked")
      };
    }

    if (typeof new_data.pending != "undefined") {
      for (var i = new_data.pending.length - 1; i >= 0; i--) {
        var seat_el = $(".seat[x="+new_data.pending[i].x+"][y="+new_data.pending[i].y+"]");
        seat_el.attr('pending', true);
        seat_el.addClass("seat_pending")
      };
    }

    update_status()
  }

  ///////////////////
  // TODO: refactor this shit!

  layout.init_ui();

  var shifted = false;
  $(document).bind('keyup keydown', function(e){shifted = e.shiftKey} );


  $("#book").click( function() {
    var selected = $(".seat[your=1]");
      /*var phone_num = prompt("Залиште свій номер телефону, ми Вам передзвонимо", "0xx-xxx-xx-xx")
      if (!phone_num) {
        alert('Без телефону нічого не вийде')
        return;
      }
      if (phone_num == "0xx-xxx-xx-xx") {
        alert("How smart! Ти часом не з Могилянки?")
        return;
      }
      */

      // TODO: get this from forms
    
      var phone_num = $("#phone").val();
      var contact_name = $("#contact_name").val();

      if (!phone_num) {
        alert('Без телефону нічого не вийде')
        return;
      }

      if (!contact_name) {
        alert('Назвались би')
        return;
      }

    
      var data = {
        seats: [],
        phone: phone_num,
        contact_name: contact_name
      }

      selected.each( function() {
        data.seats.push({
          x: $(this).attr('x'),
          y: $(this).attr('y')
        })
      })


      $("#booking_status").show();
      $("#booking_status").text('Бронюю...')
      $("#book").hide()
      $("#booking_form").hide()
        
      ajax.post('book', data, function(new_data) {

        selected.each( function() {
          $(this).toggleClass('seat_your')
          $(this).attr('your', 0)
        })

        set_data(new_data)
        seats_editable = false
        $("#legend").hide()
        $("#booking_status").html('Квиточки заброньовано, <strike>за вами виїхали</strike>&nbsp;Вам скоро передзвонять. Поки, можете перевірити чи все вірно на схемці)')
      });
  });

  $("#pre_book").click( function() {

    if ($(".seat[your=1]").length === 0 ) {
      alert('Може спочатку виберете місця?')
    } else  if ( ($(".seat[your=1]").length >= 10) && !shifted ) {
      alert('Товаришу спекулянте, більше 10 квитків в одні руки – не продаємо!')
    } else {
      $("#pre_book").hide()
      $("#booking_status").hide()
      $("#booking_form").show()
    }

  })


  set_data( data )
  seats_editable = true;


  // expose info 
  this.setSeatsInfo = set_data;

}


/////////////////////////////////
//
//  MAIN
//
if (typeof $ != "undefined") {
  $(document).ready(function() {
    var ui = new UI( {}, new Prices() ); // TODO: load prices 

    ajax.request( "get_seats", function (new_data) {
      ui.setSeatsInfo(new_data);
    });
  });
} else {
  errHandler.notLoaded();
}
