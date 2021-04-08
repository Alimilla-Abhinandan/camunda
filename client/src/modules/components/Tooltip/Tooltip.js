/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a commercial license.
 * You may not use this file except in compliance with the commercial license.
 */

import React, {useState, useRef, useEffect} from 'react';
import ReactDOM from 'react-dom';
import classnames from 'classnames';

import './Tooltip.scss';

export default function Tooltip({
  children,
  content,
  position = 'top',
  align = 'center',
  theme = 'light',
  delay = 800,
  overflowOnly = false,
}) {
  const [timeout, setTimeout] = useState();
  const [hovering, setHovering] = useState(false);
  const [style, setStyle] = useState();
  const [tooltipAlign, setTooltipAlign] = useState(align);
  const [tooltipPosition, setTooltipPosition] = useState(position);

  const hoverElement = useRef();
  const tooltip = useRef();

  useEffect(() => {
    if (!tooltip.current || !hoverElement.current) {
      return;
    }

    const body = document.fullscreenElement || document.body;
    const tooltipBox = tooltip.current.getBoundingClientRect();
    const tooltipMargin = 14; // top and bottom margin
    const tooltipHeight = tooltipBox.height + tooltipMargin;
    const hoverElementBox = hoverElement.current.getBoundingClientRect();

    const widthToArrow = align === 'center' ? tooltipBox.width / 2 : tooltipBox.width;
    const left = {
      center: hoverElementBox.x + hoverElementBox.width / 2,
      left: hoverElementBox.x,
      right: hoverElementBox.x + hoverElementBox.width,
    };

    let tooltipAlign = align;
    if (widthToArrow - left[align] > 0) {
      tooltipAlign = 'left';
    } else if (left[align] + widthToArrow > body.clientWidth) {
      tooltipAlign = 'right';
    }

    let tooltipPosition = position;
    if (position === 'bottom' && hoverElementBox.bottom + tooltipHeight > body.clientHeight) {
      tooltipPosition = 'top';
    } else if (position === 'top' && hoverElementBox.y - tooltipHeight < 0) {
      tooltipPosition = 'bottom';
    }

    setTooltipPosition(tooltipPosition);
    setTooltipAlign(tooltipAlign);
    setStyle({
      width: tooltipBox.width,
      left: left[tooltipAlign] + 'px',
      top: hoverElementBox[tooltipPosition] + 'px',
    });
  }, [align, hovering, position]);

  if (!content) {
    return children;
  }

  let useLightTheme = theme === 'light';
  if (document.body.classList.contains('dark')) {
    useLightTheme = !useLightTheme;
  }

  return (
    <>
      {React.Children.map(children, (child) =>
        React.cloneElement(child, {
          onMouseEnter: (evt) => {
            const {currentTarget} = evt;
            if (!overflowOnly || currentTarget.scrollWidth > currentTarget.clientWidth) {
              hoverElement.current = currentTarget;
              setTimeout(window.setTimeout(() => setHovering(true), delay));
            }
            child.props.onMouseEnter && child.props.onMouseEnter(evt);
          },
          onMouseLeave: (evt) => {
            hoverElement.current = null;
            setHovering(false);
            setStyle();
            window.clearTimeout(timeout);
            child.props.onMouseLeave && child.props.onMouseLeave(evt);
          },
        })
      )}
      {hovering &&
        ReactDOM.createPortal(
          <div
            className={classnames(
              'Tooltip',
              tooltipPosition,
              tooltipAlign,
              useLightTheme ? 'light' : 'dark'
            )}
            style={style}
            ref={tooltip}
          >
            {content}
          </div>,
          document.fullscreenElement || document.body
        )}
    </>
  );
}
