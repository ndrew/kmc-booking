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


// usage: log('inside coolFunc', this, arguments);
// paulirish.com/2009/log-a-lightweight-wrapper-for-consolelog/
window.log = function f(){ log.history = log.history || []; log.history.push(arguments); if(this.console) { var args = arguments, newarr; args.callee = args.callee.caller; newarr = [].slice.call(args); if (typeof console.log === 'object') log.apply.call(console.log, console, newarr); else console.log.apply(console, newarr);}};

// make it safe to use console.log always
(function(a){function b(){}for(var c="assert,count,debug,dir,dirxml,error,exception,group,groupCollapsed,groupEnd,info,log,markTimeline,profile,profileEnd,time,timeEnd,trace,warn".split(","),d;!!(d=c.pop());){a[d]=a[d]||b;}})
(function(){try{console.log();return window.console;}catch(a){return (window.console={});}}());


// place any jQuery/helper plugins in here, instead of separate, slower script files.

/**
   * Simple callback iterator
   */ 
  function Range(start, end) {  
    this.size = function() {
      return end - start
    }

    this.in = function(n) {
      return n >= start && n<=end
    }

    this.iterate = function(cb) {
      for (var i = start; i <= end; i++) {
        cb(i)
      }
    }
  }

  function Prices() {
    var prices = [
      {
        'rows': new Range(1,6),
        'seats': new Range(4,36),
        'price': 60 
      },{
        'rows': new Range(7,13),
        'seats': new Range(4,36),
        'price': 50 
      },{
        'rows': new Range(14,20),
        'seats': new Range(1,31),
        'price': 40 
      },{
        'rows': new Range(21,21),
        'seats': new Range(1,25),
        'price': 40 
      },{
        'rows': new Range(1,11),
        'seats': new Range(1,3),
        'price': 30 
      }, {
        'rows': new Range(1,11),
        'seats': new Range(37,39),
        'price': 30 
    }]

    this.getPrice = function(row, seat) {
      for (var i = prices.length - 1; i >= 0; i--) {
        var p = prices[i]
        if (p.rows.in(row) && p.seats.in(seat)) {
          return p.price;
        }
      };
      return 0;
    }

  }

function getURLParameter(name) {
    return decodeURI(
        (RegExp(name + '=' + '(.+?)(&|$)').exec(location.search)||[,null])[1]
    );
}


/**
 *
 */
function ErrorHandler() {

  this.requestFailed = function() {
    alert("Холєра, сайт не робе! Зайдіть потім")  
  };

  this.notLoaded = function() {
    alert("Can't load page! Check your internet connection and try refreshing page one time.");
  };

}

var errHandler = new ErrorHandler();

/*
 * Ajax helper
 */
function Ajax() {

  function get_url() {
      var controller_url = "main.php?" 
      if (typeof request_prefix != "undefined") {
        controller_url = request_prefix + controller_url; 
      }  
    return controller_url
  }


  this.post = function(action, data, cb) {
    $.post(get_url() + action, data, function(data) {
        cb(data);
    }, "json");
  }


  this.request = function(action, cb, err_cb) {

      var request_params = {
        url: get_url() + action,
        dataType: 'json',
        success: function(response, textStatus, jqXHR) {
          cb(response)
        },
        error: function (response, textStatus, errorThrown) {
          // TODO: scope stuff
          errHandler.requestFailed();
      }
    }
      $.ajax(request_params);
  }
  
}

var ajax = new Ajax();
